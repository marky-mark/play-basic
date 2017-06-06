package api.client

import com.typesafe.config.Config

class ServiceInfoClient(config: Config) {

  private val wsClient = {
    val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
    new play.api.libs.ws.ning.NingWSClient(builder.build())
  }

  val infoClientConfig = InfoClientConfig.infos(config)

  val serviceInfo = ServiceInfo(infoClientConfig, wsClient)
}

object ServiceInfoClient {
  def apply(config: Config): ServiceInfoClient =
    new ServiceInfoClient(config)
}
