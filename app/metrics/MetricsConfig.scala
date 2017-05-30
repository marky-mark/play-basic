package metrics

import com.typesafe.config.{Config, ConfigFactory}
import metrics.ConfigUtils._

case class MetricsConfig (
  name: String,
  environment: String,
  enabled: Boolean,
  jvm: Boolean,
  logback: Boolean,
  rateUnit: String,
  durationUnit: String,
  showSamples: Boolean,
  reporters: Map[String, MetricsReportingConfig]
)

case class MetricsReportingConfig(enabled: Boolean, config: Config)

object MetricsConfig {
  private val validUnits = Set("NANOSECONDS", "MICROSECONDS", "MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS")
  private val reporters = Set("console", "csv", "jmx")

  def apply(config: Config): MetricsConfig = {
    val specificConfig = config.getConfig("metrics")
    val reportingConfig = specificConfig.getConfig("reporting")
    val metricsConfig = MetricsConfig(
      name = specificConfig.getStringOpt("name").getOrElse("default"),
      environment = specificConfig.getString("environment"),
      enabled = specificConfig.getBooleanOpt("enabled").getOrElse(true),
      jvm = specificConfig.getBooleanOpt("jvm").getOrElse(true),
      logback = specificConfig.getBooleanOpt("logback").getOrElse(true),
      rateUnit = specificConfig.getStringOpt("rateUnit").getOrElse("SECONDS"),
      durationUnit = specificConfig.getStringOpt("durationUnit").getOrElse("MILLISECONDS"),
      showSamples = specificConfig.getBooleanOpt("showSamples").getOrElse(false),
      reporters = reporters.map(rep => rep -> reportingConfig.getConfigOpt(rep)).map { case (rep, conf) =>
        rep -> conf.fold(MetricsReportingConfig(enabled = false, ConfigFactory.empty())) { actualConf =>
          MetricsReportingConfig(
            enabled = actualConf.getBooleanOpt("enabled").getOrElse(false),
            config = actualConf
          )
        }
      }.toMap
    )
    require(validUnits.contains(metricsConfig.rateUnit))
    require(validUnits.contains(metricsConfig.durationUnit))
    metricsConfig
  }
}


