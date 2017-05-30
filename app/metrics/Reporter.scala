package metrics

import java.io.File
import com.typesafe.scalalogging._
import java.util.concurrent.TimeUnit

import com.codahale.metrics.{ConsoleReporter, CsvReporter, JmxReporter, MetricRegistry}
import com.typesafe.config.Config

import scala.util.Try

object Reporter extends LazyLogging {

  def console(conf: Config, registry: MetricRegistry): () => Any = {
    for {
      unit <- Try(conf.getString("unit")).toOption
      period <- Try(conf.getInt("period")).toOption
      prefix <- Try(conf.getString("prefix")).toOption
    } yield () => {
      logger.info("Enabling ConsoleReporter")

      ConsoleReporter.forRegistry(registry)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .convertRatesTo(TimeUnit.SECONDS)
        .build().start(period, TimeUnit.valueOf(unit))
    }
  }.getOrElse(() => Unit)

  def csv(conf: Config, registry: MetricRegistry): () => Any = {
    for {
      outputDir <- Try(conf.getString("output")).toOption
      unit <- Try(conf.getString("unit")).toOption
      period <- Try(conf.getInt("period")).toOption
      prefix <- Try(conf.getString("prefix")).toOption
    } yield () => {
      logger.info("Enabling CsvReporter")

      CsvReporter.forRegistry(registry)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .convertRatesTo(TimeUnit.SECONDS)
        .build(new File(outputDir)).start(period, TimeUnit.valueOf(unit))
    }
  }.getOrElse(() => Unit)

  def jmx(conf: Config, registry: MetricRegistry): () => Any = {
    () => JmxReporter.forRegistry(registry).build().start()
  }
}
