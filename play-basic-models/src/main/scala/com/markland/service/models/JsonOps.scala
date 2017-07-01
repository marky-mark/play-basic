package com.markland.service.models

import com.markland.service.tags._

import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.util.control.Exception.nonFatalCatch

object JsonOps {

  import com.markland.service.Id._

  implicit val jodaDateTimeReads = Reads[org.joda.time.DateTime] {
    _.validate[String].flatMap { dateStr =>
      nonFatalCatch.either(new org.joda.time.DateTime(dateStr)).fold(
        ex => JsError(Seq(JsPath() -> Seq(ValidationError(ex.getMessage)))),
        JsSuccess(_)
      )
    }
  }

  implicit val formatInfo: Format[Info] = (
    (__ \ "id").formatNullable[ids.InfoId] and
    (__ \ "name").format[String] and
    (__ \ "data").format[play.api.libs.json.JsObject] and
    (__ \ "meta").format[Seq[String]]
  )(Info.apply, unlift(Info.unapply))

  implicit val formatBatchInfo: Format[BatchInfo] =
    (__ \ "data").format[Seq[Info]].inmap(BatchInfo(_), _.data)

  implicit val formatProblem: Format[Problem] = (
    (__ \ "title").format[String] and
    (__ \ "status").format[Int] and
    (__ \ "detail").format[String] and
    (__ \ "tracking_id").formatNullable[ids.TrackingId]
  )(Problem.apply, unlift(Problem.unapply))
        

  private def createEnumFormat[T <: NamedEnum](fn: String => Either[EnumError, T]): Format[T] = {
    new Format[T] {
      override def reads(json: JsValue): JsResult[T] = {
        json.validate[String].flatMap { x =>
          fn(x).fold(
            error => JsError(Seq(JsPath() -> Seq(ValidationError(error.message)))),
            value => JsSuccess(value)
          )
        }
      }

      override def writes(o: T): JsValue = JsString(o.name)
    }
  }

  private def ammending[T](key: String, value: T)(implicit tjs: Writes[T]): JsValue => JsValue = _ match {
    case obj: JsObject => obj + (key -> Json.toJson(value))
    case jsValue => jsValue
  }

  private def readOnly[A](name: String)(implicit reads: Reads[A]): Reads[A] = {
    Reads { _ => JsError(Seq(JsPath() -> Seq(ValidationError(s"Illegal attempt to provide value for readOnly attribute '$name'")))) }
  }

  private def readOnlyFormat[A](name: String)(implicit format: Format[A]): Format[A] = {
    Format(readOnly(name), format)
  }

  private def constrainedArray[A](name: String, minItems: Option[Long], maxItems: Option[Long], uniqueItems: Option[Boolean])(implicit rds: Reads[A]): Reads[Seq[A]] = Reads { jsValue =>
    // find first dupe, rather requiring processing all elements through, e.g. "s.distinct".
    @scala.annotation.tailrec
    def hasDupe(in: Seq[A], acc: Set[A] = Set.empty[A]): Boolean = in match {
      case Nil => false
      case a +: _ if acc contains a => true
      case a +: tail                => hasDupe(tail, acc + a)
    }

    jsValue.validate[Seq[A]].flatMap { s =>
      val size = s.size
      (minItems, maxItems, uniqueItems) match {
        case (Some(min), _, _) if size < min  => JsError(s"Insufficient entries in array '$name'")
        case (_, Some(max), _) if size > max  => JsError(s"Too many entries in array '$name'")
        case (_, _, Some(true)) if hasDupe(s) => JsError(s"Non-unique items found in array '$name'")
        case _ => JsSuccess(s)
      }
    }
  }
       
}


trait TolerantReads {
  import JsonOps._
  
}
       
     
