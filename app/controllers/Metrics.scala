package controllers

import java.time.Clock

import metrics.MetricsPlugin
import play.api.Environment
import play.api.http.{HeaderNames, MimeTypes}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

class Metrics(val environment: Environment,
              metricsPlugin: MetricsPlugin)
             (implicit val ec: ExecutionContext, val clock: Clock) extends Controller {

  def metrics = Action.async { implicit request =>
    if (!metricsPlugin.enabled) Future.successful(InternalServerError("metrics plugin not enabled")) else {
      Future.successful(Ok(metricsPlugin.toJson)
        .as(MimeTypes.JSON)
        .withHeaders(HeaderNames.CACHE_CONTROL -> "must-revalidate,no-cache,no-store"))
    }
  }

}
