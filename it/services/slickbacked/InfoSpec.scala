package services.slickbacked

import java.util.UUID

import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scala.concurrent.duration._
import scala.language.postfixOps

trait InfoSpec extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with Eventually
  with DBProvidedIT {

  implicit val pc: PatienceConfig = PatienceConfig(Span(10, Seconds))
  val waitDuration = 60 seconds

  val baseSalesChannelId: UUID = UUID.fromString("75506ce9-ece6-4835-bbb1-83613c326be7")

}
