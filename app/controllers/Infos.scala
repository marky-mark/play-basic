package controllers

import java.time.Clock
import java.util.UUID

import akka.actor.ActorSystem
import com.markland.service.ContentTypes._
import com.markland.service.Id._
import com.markland.service.Cursor._
import com.markland.service.HeaderParams.RequestHeaderOps
import com.markland.service.models.JsonOps._
import com.markland.service.models._
import com.markland.service.refs.RequestGroupRef
import com.markland.service.tags.cursors.PageNextCursor
import com.markland.service.tags.ids
import com.markland.service.tags.ids.{BatchUpdateId, RequestGroupId, SalesChannelId}
import models.QueryParams
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.{Environment, Mode}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Controller, Result}
import services.InfoService
import services.events.{ProtoEventProducer, ProtoTransformer}
import services.slickbacked.SalesChannelRepository

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz._

class Infos(infoService: InfoService, salesChannelRepository: SalesChannelRepository,
            internalEventProducer: ProtoEventProducer, val environment: Environment)
           (implicit val ec: ExecutionContext, val clock: Clock) extends Controller {

  import Response._

  val secureUrl = environment.mode != Mode.Dev

  def query(limit: Int,
            nextCursor: Option[PageNextCursor] = None,
            ownerId: Option[SalesChannelId] = None) = Action.async { implicit request =>

    def paged(infos: Seq[Info], hasNext: Boolean) = InfoPage(
      next = infos.lastOption.filter(_ => hasNext).map { case i =>
        val cursor = i.id.get.value.toString.cursor[PageNext]
        PageNext(
          cursor = cursor,
          href = routes.Infos.query(limit, Some(cursor), ownerId).absoluteURL(secureUrl)
        )
      },
      items = infos
    )

    val result = for {
      _             <- RequestValidationUtils.validateLimit(limit)          |> fromEither
      infos         <- infoService.query(QueryParams(ownerId), limit + 1)   |> fromFuture
    } yield Ok(Json.toJson(paged(infos, infos.seq.size > limit))) as ApplicationInfoPageJson

    result.merge
  }

  def list(salesChannelId: ids.SalesChannelId) = Action.async { implicit request =>
    val response = for {
      lastModified      <- infoService.getLastModifiedDate(salesChannelId)       |> fromFutureOption(InfoResponses.salesChannelNotFound(salesChannelId))
      ifModifiedSince   <- fromHeaderParamError(request.ifModifiedSince) // the same as using "request.ifModifiedSince |> fromHeaderParamError"
      lastModifiedStr   <- ifModifiedSince                                       |> InfoResponses.fromLastModified(lastModified, salesChannelId)
      infos             <- infoService.list(salesChannelId)                      |> fromFuture
      acceptEncoding    <- request.acceptEncoding                                |> fromHeaderParamError
      result            <- acceptEncoding                                        |> InfoResponses.fromEncodingHeader(infos)
    } yield Ok(result).withHeaders(LAST_MODIFIED -> lastModifiedStr)

    response.merge
  }

  def post(salesChannelId: ids.SalesChannelId) = Action.async(parse.tolerantJson) { implicit request =>
    val response = for {
      salesChannelChecked <- salesChannelRepository.exists(salesChannelId.value)  |> fromFutureOption(InfoResponses.salesChannelNotFound(salesChannelId))
      body                <- request.body.validate[Info]                          |> fromJsResult
      id                  <- Future.successful(UUID.randomUUID())                 |> fromFuture
      infoId              <- infoService.insert(salesChannelId, id, body)         |> fromFutureOption(InfoResponses.issueCreatingRule())
    } yield Created(Json.obj("id" -> infoId.toString))

    response.merge
  }

  //3mb
  val MaxPostSize = 3 * 1024 * 1024

  def postBatch(salesChannelId: ids.SalesChannelId) = Action.async(parse.tolerantJson(MaxPostSize)) { implicit request =>

    val response = for {
      inGroupId       <-  request.requestGroupId                                                                 |> fromHeaderParam
      requestGroupId =    inGroupId.getOrElse(UUID.randomUUID.id[RequestGroupRef])
      body            <-  request.body.validate[BatchInfo]                                                       |> fromJsResult
      flowId          <-  request.flowId                                                                         |> fromHeaderParam
      trackingId      <-  Future.successful(UUID.randomUUID().id[UpdateInfos])                                   |> fromFuture
      bodyTransformed =   ProtoTransformer.toProto(flowId, body)
      _               <-  internalEventProducer.send(requestGroupId.value.toString, bodyTransformed.toByteArray) |> fromFuture
    } yield Accepted(Json.toJson(UpdateInfos(trackingId)))

    response.merge
  }

  def put(salesChannelId: ids.SalesChannelId, infoId: ids.InfoId) = Action.async(parse.tolerantJson) { implicit request =>
    val response = for {
      salesChannelChecked <- salesChannelRepository.exists(salesChannelId.value)  |> fromFutureOption(InfoResponses.salesChannelNotFound(salesChannelId))
      body                <- request.body.validate[Info]                          |> fromJsResult
      infoId              <- infoService.update(salesChannelId, infoId, body)     |> fromFutureOption(InfoResponses.issueUpdatingRule())
    } yield Ok(Json.obj("id" -> infoId.toString))

    response.merge
  }

  def get(salesChannelId: ids.SalesChannelId, infoId: ids.InfoId) = Action.async { implicit request =>
    val response = for {
      salesChannelChecked <- salesChannelRepository.exists(salesChannelId.value)  |> fromFutureOption(InfoResponses.salesChannelNotFound(salesChannelId))
      info                <- infoService.retrieve(salesChannelId, infoId)         |> fromFutureOption(InfoResponses.issueUpdatingRule())
    } yield Ok(Json.toJson(info))

    response.merge
  }
}

object InfoResponses {

  import play.api.http.HeaderNames.CONTENT_ENCODING
  import play.api.mvc.Results._

  val dateTimePattern = "E, dd MMM yyyy kk:mm:ss z"
  val dtFormat = DateTimeFormat.forPattern(dateTimePattern)

  private val GZIP_ENCODING = "gzip"

  def salesChannelNotFound(salesChannelId: SalesChannelId) = {
    val problem = Problem(title = "Not Found", status = 404, detail = s"Sales channel ${salesChannelId.value.toString} does not exist")
    NotFound(Json.toJson(Seq(problem)))
  }

  def issueCreatingRule() = {
    val problem = Problem(title = "Internal Server Error", status = 500, detail = "Issue Creating Rule")
    InternalServerError(Json.toJson(Seq(problem)))
  }

  def issueUpdatingRule() = {
    val problem = Problem(title = "Internal Server Error", status = 500, detail = "Issue Updating Rule")
    InternalServerError(Json.toJson(Seq(problem)))
  }

  def fromEncodingHeader(infos: Seq[Info])(acceptEncoding: Option[String]) = EitherT[Future, Result, JsValue] {
    val json = Json.toJson(infos)
    acceptEncoding match {
      case None => Future.successful(json.right)
      case Some(encoding) if (encoding == GZIP_ENCODING) => {
        val compressed = Gzip.encode(json.toString.getBytes)
        Future.successful(Ok(compressed).withHeaders(CONTENT_ENCODING -> GZIP_ENCODING).left)
      }
      case Some(encoding) => Future.successful(json.right)
    }
  }

  def fromLastModified(lastModified: DateTime, salesChannelId: ids.SalesChannelId)( ifModifiedSince: Option[String]) = EitherT[Future, Result, String] {
    val lastModifiedStr = dtFormat.print(lastModified)
    val res = ifModifiedSince match {
      case Some(str) => {
        try {
          val modifiedSince = DateTime.parse(str, dtFormat)
          val lastModifiedFormatted = DateTime.parse(lastModifiedStr, dtFormat)
          if (modifiedSince.getMillis == lastModifiedFormatted.getMillis) NotModified.left
          else lastModifiedStr.right
        }
        catch {
          case iag: IllegalArgumentException => {
            val problem = Problem(title = "Bad Request", status = 400, detail = s"Failed to parse If-Modified-Since header. The datetime format is: $dateTimePattern")
            BadRequest(Json.toJson(Seq(problem))).left
          }
        }
      }
      case None => lastModifiedStr.right
    }
    Future.successful(res)
  }
}
