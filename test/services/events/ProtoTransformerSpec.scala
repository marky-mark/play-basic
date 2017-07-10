package services.events

import java.util.UUID

import org.scalatest._
import com.markland.service.Id.IdOps
import com.markland.service.models.{InfoStatusEnum, BatchInfo => ModelBatchInfo, Info => ModelInfo}
import com.markland.service.refs.FlowRef
import com.markland.service.tags.ids.FlowId
import play.api.libs.json.{JsObject => PlayJsonObject}

class ProtoTransformerSpec extends FlatSpec with Matchers {

  def dataJson = play.api.libs.json.Json.parse(
    s"""
       |{
       |   "name": "val"
       |
       |}
    """.stripMargin
  )

  it should "Transform to ProtoBuf Object" in {
    val testInfo = ModelInfo(Some(UUID.fromString("62729342-A89D-401A-8B42-32BD15E01220").id), "bar", dataJson.as[PlayJsonObject], List("foo", "bar"), InfoStatusEnum.Active)
    val batchInfo = ModelBatchInfo(Seq(testInfo))
    val flowId: FlowId = UUID.randomUUID().toString.id[FlowRef]
    val protoBatch: BatchInfo = ProtoTransformer.toProto(Some(flowId), batchInfo)

    protoBatch.flowId should === (flowId.value)
    protoBatch.info.head.id should === (testInfo.id.get.value.toString)
    protoBatch.info.head.meta should === (testInfo.meta)
    protoBatch.info.head.data.get.entries.head.value.get.value.jsString.get.value should === ("val")
  }
}
