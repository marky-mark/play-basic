package controllers

import com.markland.service.HeaderParams
import com.markland.service.HeaderParams.HeaderParamError
import com.markland.service.models.Problem
import models.Models.{Problems, ServiceError, ValidationError => InternalValidationError}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsResult, Json}
import play.api.mvc.{Result, Results}
import play.api.mvc.Results._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Either
import scalaz._
import Scalaz._


object Response  {

  import com.markland.service.models.JsonOps._

  def fromJsResult[T](result: JsResult[T]) = EitherT[Future, Result, T] {

    def toP(jsPath: JsPath, validationError: ValidationError): Problem =
      Problems.invalidInputProblem(s"${jsPath.toString}: ${validationError.message}")

    def convert(errors: Seq[(JsPath, Seq[ValidationError])]): Seq[Problem] = {
      errors.flatMap { case (path, verrors) => verrors.map(toP(path, _)) }
    }

    Future.successful {
      result.asEither.disjunction.leftMap {
        (errors: Seq[(JsPath, Seq[ValidationError])]) => BadRequest(Json.toJson(convert(errors)))
      }
    }
  }

  def fromValidationResult[T](result: \/[Seq[ServiceError], T]): EitherT[Future, Result, T] = EitherT {
    Future.successful {
      result.leftMap { errors =>
        val problems = errors.map(e => Problem(title = "Invalid Input", status = 400, detail = e.message))
        BadRequest(Json.toJson(problems))
      }
    }
  }

  def fromEitherT[T](result: EitherT[Future, ServiceError, T]): EitherT[Future, Result, T] = result.leftMap {
      case error if error.`type` == InternalValidationError => BadRequest(Json.toJson(Seq(Problem(title = "Validation Error", status = 400, detail = error.message))))
      case error => InternalServerError(Json.toJson(Seq(Problem(title = "Internal Server Error", status = 500, detail = error.message))))
  }

  def fromEither[T](either: Problem \/ T): EitherT[Future, Result, T] = EitherT[Future, Result, T] {
    Future.successful(either.leftMap(p => Results.BadRequest(Json.toJson(Seq(p)))))
  }

  def fromFuture[T](fut: Future[T])(implicit ec: ExecutionContext) = EitherT[Future, Result, T] { fut.map(_.right[Result]) }

  def fromFutureOption[T](onNone: => Result)(source: Future[Option[T]])(implicit ec: ExecutionContext) = EitherT[Future, Result, T] {
    source.map(_ \/> onNone)
  }

  def toProblem(error: HeaderParamError): Problem = {
    val detail = error match {
      case HeaderParams.MissingParam(name) => s"Missing header parameter '$name'"
      case HeaderParams.EnumValue(message) => message
      case HeaderParams.ConversionError(ex) => s"Failed to convert header parameter value: ${ex.getMessage}"
    }
    Problems.invalidInputProblem(detail)
  }

  def fromHeaderParam[T](either: scala.util.Either[HeaderParamError, T]) = EitherT[Future, Result, T] {
    Future.successful(either.disjunction.leftMap(toProblem).leftMap(p => Results.BadRequest(Json.toJson(Seq(p)))))
  }

  def fromHeaderParamError[T](result: Either[HeaderParamError, T]): EitherT[Future, Result, T] = EitherT[Future, Result, T] {

    def toProblem(error: HeaderParamError): Problem = error match {
      case HeaderParams.MissingParam(name) => Problems.invalidInputProblem(s"Missing Parameter: $name")
      case HeaderParams.EnumValue(message) => Problems.invalidInputProblem(message)
      case HeaderParams.ConversionError(ex) => Problems.invalidInputProblem(s"Failed to convert input for parameter: ${ex.getMessage}")
    }

    val disjunction = \/.fromEither(result)
    Future.successful(disjunction.leftMap { error =>
      BadRequest(Json.toJson(Seq(toProblem(error))))
    })
  }

}
