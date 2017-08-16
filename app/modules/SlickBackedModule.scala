package modules

import com.softwaremill.macwire._
import play.api.Configuration
import services.slickbacked.{EventTrackingRepositoryImpl, DatabaseProvider, InfoRepositoryImpl, SalesChannelRepositoryImpl}

trait SlickBackedModule { self: MetricsModule =>

  def configuration: Configuration

  lazy val db = wire[DatabaseProvider]

  lazy val salesChannelRepository = wire[SalesChannelRepositoryImpl]
  lazy val infoRepository = wire[InfoRepositoryImpl]
  lazy val eventTrackingRepository = wire[EventTrackingRepositoryImpl]
}
