package modules

import play.api.Configuration
import services.InfoServiceImpl
import com.softwaremill.macwire._

trait ServiceModule { self: SlickBackedModule =>
  def configuration: Configuration

  lazy val infoService = wire[InfoServiceImpl]

}
