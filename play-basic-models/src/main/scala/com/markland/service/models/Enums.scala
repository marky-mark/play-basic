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


     
