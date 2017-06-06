package api.client

import com.typesafe.config.Config

trait InfoClientConfig {
  def baseUrl: String
}

object InfoClientConfig {

  def infos(config: Config): InfoClientConfig =
    apply(config, "info-client")

  private def apply[S](config: Config, name: String): InfoClientConfig = {
    val servicesConfig = config.getConfig("external-services")
    val serviceConfig = servicesConfig.getConfig(name)
    new InfoClientConfig {
      override val baseUrl = serviceConfig.getString("base-url")
    }
  }
}
