package services.slickbacked

import java.util.UUID

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scala.concurrent.duration._
import scala.language.postfixOps

trait DBSpec extends FlatSpec
  with BaseSetup
  with Matchers
  with ScalaFutures
  with OptionValues
  with DBProvided {

  implicit val pc: PatienceConfig = PatienceConfig(Span(2, Seconds))
  val waitDuration = 10 seconds

  val salesChannelRepository = new SalesChannelRepositoryImpl(databaseProvider)
  val infoRepository = new InfoRepositoryImpl(databaseProvider)

  val baseSalesChannelId: UUID = UUID.fromString("F23E89E7-4162-432D-A7B0-F00181CF76FE")

}
