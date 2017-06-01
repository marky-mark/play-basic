package services.slickbacked

import java.util.UUID

import metrics.{MetricsConfig, MetricsServiceImpl}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scala.concurrent.duration._
import scala.language.postfixOps

trait DBSpec extends FlatSpec
  with BaseSetup
  with Matchers
  with ScalaFutures
  with OptionValues
  with DBProvided
  with MockitoSugar {

  implicit val pc: PatienceConfig = PatienceConfig(Span(2, Seconds))
  val waitDuration = 10 seconds

  val mockMetricService = new MetricsServiceImpl(MetricsConfig(configuration.underlying))

  val salesChannelRepository = new SalesChannelRepositoryImpl(databaseProvider)
  val infoRepository = new InfoRepositoryImpl(databaseProvider, mockMetricService)

  val baseSalesChannelId: UUID = UUID.fromString("75506ce9-ece6-4835-bbb1-83613c326be7")

}
