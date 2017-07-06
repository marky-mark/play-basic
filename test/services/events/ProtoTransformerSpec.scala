package services.events

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.JsObject

class ProtoTransformerSpec extends FlatSpec with Matchers {

  def dataJson = play.api.libs.json.Json.parse(
    s"""
       |{
       |   "foo": "bar"
       |
       |}
    """.stripMargin
  )

  it should "Transform to ProtoBuf Object" = {
    val testInfo = Info("foo", "bar", Some(dataJson.as[JsObject]), List("foo", "bar"))
    val batchInfo = BatchInfo("foo", Seq(testInfo))
    val protoBatch = ProtoTransformer.toProto(batchInfo)

    protoBatch.flowId should === batchInfo
  }
}
