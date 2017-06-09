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


     
