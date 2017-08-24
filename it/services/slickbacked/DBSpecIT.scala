package services.slickbacked

import metrics.{MetricsService, MetricsConfig, MetricsServiceImpl}
import setup.BaseITSetup

class DBSpecIT extends InfoSpec with BaseITSetup with EhCacheProvided {

  val metricService = new MetricsServiceImpl(MetricsConfig(configuration.underlying))

  val salesChannelRepository = new SalesChannelRepositoryImpl(databaseProvider, cachingContext)
  val infoRepository = new InfoRepositoryImpl(databaseProvider, metricService)

  override def metricsService: MetricsService = metricService
}
