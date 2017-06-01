package controllers

import java.time.Clock
import java.util.UUID

import api.service.HeaderParams.RequestHeaderOps
import api.service.models.JsonOps._
import api.service.models.{Info, Problem}
import api.service.tags.ids
import api.service.tags.ids.SalesChannelId
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Controller, Result}
import services.InfoService
import services.slickbacked.SalesChannelRepository

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz._

class Infos(infoService: InfoService, salesChannelRepository: SalesChannelRepository)
           (implicit val ec: ExecutionContext, val clock: Clock) extends Controller {

  import Response._

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
    val problem = Problem(title = "Internal Server Error", status = 500, detail = "Issue Cresting Rule")
    InternalServerError(Json.toJson(Seq(problem)))
  }

  def fromEncodingHeader(rules: Seq[Info])(acceptEncoding: Option[String]) = EitherT[Future, Result, JsValue] {
    val json = Json.toJson(rules)
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
