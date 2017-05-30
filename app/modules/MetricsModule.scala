package modules

import metrics.{MetricsConfig, MetricsPlugin, MetricsServiceImpl}

trait MetricsModule { self: ServiceModule =>

  lazy val metricsConfig = MetricsConfig(configuration.underlying)
  val metricsPlugin = new MetricsPlugin(metricsConfig)

  val metricService = new MetricsServiceImpl(metricsConfig)
}
