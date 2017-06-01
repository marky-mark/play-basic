import java.util.UUID

import api.common.Id._
import api.service.models._
import api.service.refs.TrackingRef
import com.typesafe.scalalogging.LazyLogging
import models.Models.Problems
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.Future


class ErrorHandler extends HttpErrorHandler with LazyLogging {

  import api.service.models.JsonOps._
  import play.api.http.Status

  sealed trait ErrorCode

  object ErrorCode {
    case object RequestError extends ErrorCode
    case object InternalError extends ErrorCode
  }

  def asProblem(httpStatus: Int, code: ErrorCode, description: String, trackingId: Option[UUID]): Problem = {
    code match {
      case ErrorCode.InternalError => Problems.internalErrorProblem(description, trackingId = trackingId.map(_.id[TrackingRef]))
      case ErrorCode.RequestError => Problems.invalidInputProblem(description, trackingId = trackingId.map(_.id[TrackingRef]))
    }
  }

  val errorContentType: String = "application/json"

  override def onClientError(request: RequestHeader, statusCode: Int, message: String = ""): Future[Result] = {

    val transformedResult = statusCode match {
      case Status.NOT_FOUND => Results.Unauthorized
      case _ => new Results.Status(statusCode).apply {
        Json.toJson(Seq(asProblem(statusCode, ErrorCode.RequestError, message, None)))
      } as errorContentType
    }

    Future.successful(transformedResult)
  }

  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    val uuid = UUID.randomUUID()
    logger.error(s"Unhandled exception as InternalServerError for ${request.method} on ${request.path} with id $uuid", ex)
    Future.successful {
      Results.InternalServerError {
        Json.toJson(Seq(asProblem(Status.INTERNAL_SERVER_ERROR, ErrorCode.InternalError, "Internal server error - please contact service team for details", Some(uuid))))
      } as errorContentType
    }
  }
}
