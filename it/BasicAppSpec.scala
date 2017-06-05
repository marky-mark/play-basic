import java.util.UUID

import api.service.models.Info
import com.typesafe.config.{Config, ConfigFactory}
import play.api.libs.json.{JsObject, Json}
import services.slickbacked.InfoSpec

import scalaj.http.{Http, HttpResponse}
import api.service.models.JsonOps._

class BasicAppSpec extends InfoSpec {

  val config: Config = ConfigFactory.load("application.it.conf")
  val baseUrl = config.getString("url")

  it should "return OK for health check" in {

    eventually {
      val output = Http(s"http://$baseUrl:9000/health").asString
      output.isSuccess shouldBe true
    }

  }

  it should "return Created for posting an object" in {

    def dataJson = Json.parse("{\"foo\":\"bar\"}")

    eventually {
      val infoToCreate = Json.toJson(new Info(name = "foo", data = dataJson.as[JsObject], meta = Seq("foo"))).toString()
      val output: HttpResponse[String] = Http(s"http://$baseUrl:9000/api/sales-channels/$baseSalesChannelId/infos")
        .postData(infoToCreate).asString

      output.code shouldBe 201
      val result = (Json.parse(output.body) \ "id").as[String]
      println(s"Info created Id - $result")

      //will throw an exeception if not a UUID
      val id = UUID.fromString(result)
      id shouldBe a [UUID]
    }
  }

}
