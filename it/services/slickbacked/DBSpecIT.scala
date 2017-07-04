package services.slickbacked

import metrics.{MetricsConfig, MetricsServiceImpl}
import setup.BaseITSetup

class DBSpecIT extends InfoSpec with BaseITSetup {

  val metricService = new MetricsServiceImpl(MetricsConfig(configuration.underlying))

  val salesChannelRepository = new SalesChannelRepositoryImpl(databaseProvider)
  val infoRepository = new InfoRepositoryImpl(databaseProvider, metricService)
}
