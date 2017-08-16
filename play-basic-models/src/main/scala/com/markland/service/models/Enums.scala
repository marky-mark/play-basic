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

object EnrichmentUpdateStatusStatusEnum {
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


     
