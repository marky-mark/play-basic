package modules

import play.api.Configuration

trait ServiceModule {
  def configuration: Configuration


}
