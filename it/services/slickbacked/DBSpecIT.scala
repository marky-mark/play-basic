package services.slickbacked

import metrics.{MetricsConfig, MetricsServiceImpl}
import setup.BaseSetup

class DBSpecIT extends InfoSpec with BaseSetup {

  val metricService = new MetricsServiceImpl(MetricsConfig(configuration.underlying))

  val salesChannelRepository = new SalesChannelRepositoryImpl(databaseProvider)
  val infoRepository = new InfoRepositoryImpl(databaseProvider, metricService)
}
