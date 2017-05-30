package filters

import akka.stream.Materializer
import metrics.MetricsService
import nl.grons.metrics.scala.DefaultInstrumented
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

object Metrics {
}

class MetricsFilter(metricsService: MetricsService)(implicit ec: ExecutionContext, val mat: Materializer) extends Filter with DefaultInstrumented {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val method = requestHeader.tags.get(play.api.routing.Router.Tags.RouteActionMethod)
    val controller = requestHeader.tags.get(play.api.routing.Router.Tags.RouteController)

    val startTime = DateTime.now()
    if (method.isEmpty || controller.isEmpty) {
      Logger.error(s"Error: cannot get method or controller name while filtering metrics. Method_name: $method, controller_name: $controller")
      nextFilter(requestHeader)
    } else {
      nextFilter(requestHeader).andThen {
        case Success(response) if response.header.status / 100 == 2 =>
          val name = s"api.response.2xx.${requestHeader.method}.${controller.get}.${method.get}"
          metricsService.measureLatency(name, startTime)
        case _ =>
      }
    }
  }
}
