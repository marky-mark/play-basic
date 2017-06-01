package models

import api.service.models.Problem
import api.service.tags.ids._
import play.api.http.Status

object Models {

  sealed trait CreateResult
  object CreateResult {
    case object Inserted extends CreateResult
    case object Conflict extends CreateResult
  }

  object Problems {
    def internalErrorProblem(detail: String, status: Int = Status.INTERNAL_SERVER_ERROR, trackingId: Option[TrackingId] = None) = {
      Problem(
        status = status,
        title = "Internal Server Error",
        detail = detail,
        trackingId = trackingId
      )
    }

    def invalidInputProblem(detail: String, status: Int = Status.BAD_REQUEST, trackingId: Option[TrackingId] = None) = {
      Problem(
        status = status,
        title = "Invalid Input",
        detail = detail,
        trackingId = trackingId
      )
    }
  }

  sealed trait ServiceErrorType

  case object DbError extends ServiceErrorType
  case object ValidationError extends ServiceErrorType

  case class ServiceError(`type`: ServiceErrorType, message: String, detail: Option[String] = None)

}