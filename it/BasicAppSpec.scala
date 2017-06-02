import com.typesafe.config.{Config, ConfigFactory}
import services.slickbacked.InfoSpec

import scalaj.http.Http

class BasicAppSpec extends InfoSpec {

  val config: Config = ConfigFactory.load("application.it.conf")
  val baseUrl = config.getString("url")

  "Health" should "return OK for health check" in {

    eventually {
      val output = Http(s"http://$baseUrl:9000/health").asString
      output.isSuccess shouldBe true
    }

  }

}
