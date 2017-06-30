package controllers

import java.io.Writer

import akka.util.ByteString
import io.prometheus.client._
import io.prometheus.client.exporter.common.TextFormat
import metrics.MetricsPlugin
import play.api.http.HttpEntity
import play.api.mvc.{Action, Controller, ResponseHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

class PrometheusMetrics(metricsPlugin: MetricsPlugin)(implicit val ec: ExecutionContext) extends Controller {

  def metrics = Action.async { implicit request =>
    if (!metricsPlugin.enabled && !prometheusEnabled) Future.successful(InternalServerError("prometheus not enabled")) else {

      val samples = new StringBuilder()
      val writer = new WriterAdapter(samples)
      TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())
      writer.close()

      Future.successful(Result(
        header = ResponseHeader(200, Map.empty),
        body = HttpEntity.Strict(ByteString(samples.toString), Some(TextFormat.CONTENT_TYPE_004))
      ))
    }
  }

  private def prometheusEnabled : Boolean = {
    metricsPlugin.config.reporters.get("prometheus") match {
      case Some(c) => c.enabled
      case None => false
    }
  }

  class WriterAdapter(buffer: StringBuilder) extends Writer {

    override def write(charArray: Array[Char], offset: Int, length: Int): Unit = {
      buffer ++= new String(new String(charArray, offset, length).getBytes("UTF-8"), "UTF-8")
    }

    override def flush(): Unit = {}

    override def close(): Unit = {}
  }

}
