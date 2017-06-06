
package api.service

import models._
import tags.ids._


import play.api.mvc.RequestHeader

import scala.util.control.Exception.nonFatalCatch
import scala.util.{Either, Left, Right}

object HeaderParams {
  sealed trait HeaderParamError
  case class MissingParam(name: String) extends HeaderParamError
  case class EnumValue(message: String) extends HeaderParamError
  case class ConversionError(ex: Throwable) extends HeaderParamError

  implicit class RequestHeaderOps(val request: RequestHeader) extends AnyVal {
    def flowId(): Either[HeaderParamError, Option[FlowId]] = {
      get("X-Flow-Id").right.map(_.map(new FlowId(_)))
    }

    def acceptEncoding(): Either[HeaderParamError, Option[String]] = {
      get("Accept-Encoding")
    }

    def ifModifiedSince(): Either[HeaderParamError, Option[String]] = {
      get("If-Modified-Since")
    }

    private def get(name: String): Either[HeaderParamError, Option[String]] = {
      // FIXME - shouldn't this case insensitive???
      Right(request.headers.get(name).filterNot(_.isEmpty))
    }

    private def getMandatory(name: String, ifMissing: => Option[String] = None): Either[HeaderParamError, String] = {
      get(name).right.flatMap {
        _.orElse(ifMissing) match {
          case None => Left(MissingParam(name))
          case Some(value) => Right(value)
        }
      }
    }

  }
}
     
