package modules

import com.softwaremill.macwire._
import play.api.Configuration
import services.slickbacked.{DatabaseProvider, InfoRepositoryImpl, SalesChannelRepositoryImpl}

trait SlickBackedModule {

  def configuration: Configuration

  lazy val db = wire[DatabaseProvider]

  lazy val salesChannelRepository = wire[SalesChannelRepositoryImpl]
  lazy val infoRepository = wire[InfoRepositoryImpl]
}
