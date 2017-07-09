import java.util.UUID

import akka.actor.ActorSystem
import api.client.ServiceInfoClient
import com.markland.service.models.{BatchInfo, Info}
import com.markland.service.models.JsonOps._
import com.markland.service.refs.{FlowRef, SalesChannelRef}
import com.markland.service.tags.ids.FlowId
import com.markland.service.Id._
import com.typesafe.config.{Config, ConfigFactory}
import models.Models.ServiceError
import play.api.libs.json.{JsObject, Json}
import services.slickbacked.InfoSpec

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Success, Try}
import scalaj.http.{Http, HttpResponse}
import scalaz._
import Scalaz._

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

  private def genFlowId(): FlowId =
    UUID.randomUUID().toString.id[FlowRef]

  it should "Using client" in {

    val client = ServiceInfoClient(config)

    implicit lazy val system: ActorSystem = ActorSystem("test-system", config)
    implicit lazy val context = system.dispatcher

    eventually {
      val futureResult: EitherT[Future, ServiceError, Iterable[Info]] = client.serviceInfo.list(baseSalesChannelId.id[SalesChannelRef], Some(genFlowId()))

      val res: Try[Either[ServiceError, Iterable[Info]]] = Await.ready(futureResult.toEither, Duration.Inf).value.get

      println(s"FOOOO! $res")
      res match {
        case Success(i) => i.isLeft shouldBe true; i match {
          case Left(in) => in.message shouldBe "Response code: 404. Reason: Not Found"
          case _ =>
        }
        case _ =>
      }
    }
  }

  it should "Batch requests onto a bus" in {
    def dataJson = Json.parse("{\"val\":\"ue\"}")
    def dataJson2 = Json.parse("{\"val2\":\"ue2\"}")

    eventually {
      val infoSeq: Seq[Info] = Seq(new Info(name = "foo", data = dataJson.as[JsObject], meta = Seq("meta")),
        new Info(name = "foo2", data = dataJson2.as[JsObject], meta = Seq("meta2")))
      val infoToCreate = Json.toJson(new BatchInfo(infoSeq)).toString()
      val output: HttpResponse[String] = Http(s"http://$baseUrl:9000/api/batch/sales-channels/$baseSalesChannelId/infos")
        .postData(infoToCreate).asString

      output.code shouldBe 202
      val result = (Json.parse(output.body) \ "tracking_id").as[String]
      println(s"Info created Id - $result")

      //will throw an exeception if not a UUID
      val id = UUID.fromString(result)
      id shouldBe a [UUID]
    }
  }

}
