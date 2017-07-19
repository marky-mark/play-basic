package services.events

import java.util.UUID

import org.scalatest._
import com.markland.service.Id.IdOps
import com.markland.service.models.{InfoStatusEnum, BatchInfo => ModelBatchInfo, Info => ModelInfo}
import com.markland.service.refs.{FlowRef, SalesChannelRef}
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
    val testInfo = ModelInfo(Some(UUID.fromString("62729342-A89D-401A-8B42-32BD15E01220").id), "bar",
      dataJson.as[PlayJsonObject], List("foo", "bar"), InfoStatusEnum.Inactive, Some(org.joda.time.DateTime.now()))
    val batchInfo = ModelBatchInfo(Seq(testInfo))
    val flowId: FlowId = UUID.randomUUID().toString.id[FlowRef]
    val salesChannelId: UUID = UUID.fromString("9C9DD362-9581-43D2-B2E1-C9209A94B7E7")
    val protoBatch: BatchInfo = ProtoTransformer.toProto(Some(flowId), batchInfo,
      salesChannelId.id[SalesChannelRef])

    protoBatch.flowId should === (flowId.value)
    protoBatch.salesChannelId should === (salesChannelId.toString)
    protoBatch.info.head.id should === (testInfo.id.get.value.toString)
    protoBatch.info.head.meta should === (testInfo.meta)
    protoBatch.info.head.status.isActive should === (false)
    protoBatch.info.head.status.isActive should not be true
    protoBatch.info.head.data.get.entries.head.value.get.value.jsString.get.value should === ("val")
    new org.joda.time.DateTime(protoBatch.info.head.lastModified.get.time) should === (testInfo.lastModified.get)

    val result = ProtoTransformer.fromProto(protoBatch)
    result._1.get should === (flowId)
    result._2 should === (batchInfo)
    result._3.value should === (salesChannelId)
  }

  it should "Transform to ProtoBuf Object failing test" in {
    val testInfo = ModelInfo(None, "bar",
      play.api.libs.json.Json.parse("{}").as[PlayJsonObject], List("foo"), InfoStatusEnum.Active)
    val batchInfo = ModelBatchInfo(Seq(testInfo))
    val flowId: FlowId = UUID.randomUUID().toString.id[FlowRef]
    val protoBatch: BatchInfo = ProtoTransformer.toProto(Some(flowId), batchInfo,
      UUID.fromString("9C9DD362-9581-43D2-B2E1-C9209A94B7E7").id[SalesChannelRef])

    protoBatch.flowId should === (flowId.value)
    protoBatch.info.size should === (1)
  }
}
