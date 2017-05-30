package metrics

import java.io.StringWriter
import java.util.concurrent.TimeUnit

import ch.qos.logback.classic.LoggerContext
import com.codahale.metrics.json.MetricsModule
import com.codahale.metrics.jvm.{GarbageCollectorMetricSet, MemoryUsageGaugeSet, ThreadStatesGaugeSet}
import com.codahale.metrics.logback.InstrumentedAppender
import com.codahale.metrics.{MetricRegistry, SharedMetricRegistries}
import com.fasterxml.jackson.databind.{ObjectMapper, ObjectWriter}
import nl.grons.metrics.scala.DefaultInstrumented
import org.slf4j.LoggerFactory

import scala.language.implicitConversions


class MetricsPlugin(metricsConfig: MetricsConfig) extends DefaultInstrumented {

  val mapper: ObjectMapper = new ObjectMapper()

  implicit def stringToTimeUnit(s: String): TimeUnit = TimeUnit.valueOf(s)

  def start(): Any = {
    def setupJvmMetrics(registry: MetricRegistry): Unit = {
      if (metricsConfig.jvm) {
        registry.registerAll(new GarbageCollectorMetricSet())
        registry.registerAll(new MemoryUsageGaugeSet())
        registry.registerAll(new ThreadStatesGaugeSet())
      }
    }

    def setupLogbackMetrics(registry: MetricRegistry): Unit = {
      if (metricsConfig.logback) {
        val appender: InstrumentedAppender = new InstrumentedAppender(registry)

        val factory = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
        val rootLogger = factory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)

        appender.setContext(rootLogger.getLoggerContext)
        appender.start()
        rootLogger.addAppender(appender)
      }
    }

    def setupReporting(conf: Map[String, MetricsReportingConfig], registry: MetricRegistry): Unit =
      Map(
        "console" -> Reporter.console _,
        "csv" -> Reporter.csv _,
        "jmx" -> Reporter.jmx _
      ).foreach {
        case (name, fun) =>
          conf.get(name).foreach { cfg =>
            if (cfg.enabled) {
              fun(cfg.config, metricRegistry)()
            }
          }
      }

    if (enabled) {
      setupJvmMetrics(metricRegistry)
      setupLogbackMetrics(metricRegistry)
      setupReporting(metricsConfig.reporters, metricRegistry)

      val module = new MetricsModule(metricsConfig.rateUnit, metricsConfig.durationUnit, metricsConfig.showSamples)
      mapper.registerModule(module)
    }
  }

  def enabled: Boolean = metricsConfig.enabled

  def stop(): Unit = SharedMetricRegistries.remove(metricsConfig.name)

  def toJson: String = {
    val writer: ObjectWriter = mapper.writer()
    val stringWriter = new StringWriter()
    writer.writeValue(stringWriter, metricRegistry)
    stringWriter.toString
  }
}
