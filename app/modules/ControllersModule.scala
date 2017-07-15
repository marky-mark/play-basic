package modules

import java.time.Clock

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.softwaremill.macwire._
import controllers._
import filters.MetricsFilter
import play.api.Environment

import scala.concurrent.ExecutionContext

trait ControllersModule { self: ServiceModule with MetricsModule with SlickBackedModule with EventsProducerModule =>

  def environment: Environment

  private implicit lazy val applicationContext: ExecutionContext = ActorSystem("controllers-context", configuration.underlying).dispatcher

  private implicit lazy val clock = Clock.systemUTC()

  implicit val system = ActorSystem()

  private implicit lazy val mat: Materializer = ActorMaterializer()

  lazy val metricsFilter = wire[MetricsFilter]

  lazy val health = wire[HealthCheck]
  lazy val metrics = wire[Metrics]
  lazy val prometheusMetrics = wire[PrometheusMetrics]

  lazy val info = wire[Infos]

}
