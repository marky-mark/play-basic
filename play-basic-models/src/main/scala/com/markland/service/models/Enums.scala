package com.markland.service.models

import scala.util.{Either, Left, Right}

trait EnumError {
  def message: String
}
case class BadValue(message: String) extends EnumError

sealed trait NamedEnum {
  def name: String
  override def toString(): String = name
}

object InfoStatusEnum {
  sealed trait InfoStatus extends NamedEnum

  case object Active extends InfoStatus { override val name = "active" }
  case object Inactive extends InfoStatus { override val name = "inactive" }

  def apply(name: String): Either[EnumError, InfoStatus] = name match {
    case Active.name => Right(Active)
    case Inactive.name => Right(Inactive)
    case _ => Left(BadValue(s"Unknown value '$name' for 'InfoStatus' enum"))
  }
}

object BatchInfoUpdateStatusStatusEnum {
  sealed trait Status extends NamedEnum

  case object Pending extends Status { override val name = "pending" }
  case object Successful extends Status { override val name = "successful" }
  case object Failed extends Status { override val name = "failed" }


  def apply(name: String): Either[EnumError, Status] = name match {
    case Pending.name => Right(Pending)
    case Successful.name => Right(Successful)
    case Failed.name => Right(Failed)
    case _ => Left(BadValue(s"Unknown value '$name' for 'status' enum"))
  }
}

object BatchInfoUpdateStatusResultEnum {
  sealed trait Result extends NamedEnum

  case object Inserted extends Result { override val name = "inserted" }
  case object Updated extends Result { override val name = "updated" }
  case object Missing extends Result { override val name = "missing" }
  case object InputError extends Result { override val name = "input_error" }
  case object AuthorizationError extends Result { override val name = "authorization_error" }
  case object ForbiddenError extends Result { override val name = "forbidden_error" }
  case object ValidationError extends Result { override val name = "validation_error" }
  case object InternalError extends Result { override val name = "internal_error" }


  def apply(name: String): Either[EnumError, Result] = name match {
    case Inserted.name => Right(Inserted)
    case Updated.name => Right(Updated)
    case Missing.name => Right(Missing)
    case InputError.name => Right(InputError)
    case AuthorizationError.name => Right(AuthorizationError)
    case ForbiddenError.name => Right(ForbiddenError)
    case ValidationError.name => Right(ValidationError)
    case InternalError.name => Right(InternalError)
    case _ => Left(BadValue(s"Unknown value '$name' for 'result' enum"))
  }
}


     
