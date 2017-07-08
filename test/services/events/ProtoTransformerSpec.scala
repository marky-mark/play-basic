package services.events

import java.util.UUID

import org.scalatest._
import com.markland.service.Id.IdOps
import com.markland.service.models.{BatchInfo => ModelBatchInfo, Info => ModelInfo}
import play.api.libs.json.{JsObject => PlayJsonObject}

class ProtoTransformerSpec extends FlatSpec with Matchers {

  def dataJson = play.api.libs.json.Json.parse(
    s"""
       |{
       |   "foo": "bar"
       |
       |}
    """.stripMargin
  )

  it should "Transform to ProtoBuf Object" in {
    val testInfo = ModelInfo(Some(UUID.fromString("62729342-A89D-401A-8B42-32BD15E01220").id), "bar", dataJson.as[PlayJsonObject], List("foo", "bar"))
    val batchInfo = ModelBatchInfo(Seq(testInfo))
    val protoBatch: BatchInfo = ProtoTransformer.toProto("flowId", batchInfo)

    protoBatch.flowId should === ("flowId")
    protoBatch.info.head.id should === (testInfo.id.get.value.toString)
    protoBatch.info.head.meta should === (testInfo.meta)
  }
}
