package services.events

import com.google.protobuf.ByteString
import com.markland.service.models.{BatchInfo => ModelBatchInfo, Info => ModelInfo}
import play.api.libs.json.{JsArray => PlayJsArray, JsValue => PlayJsValue, JsObject => PlayJsonObject,
JsString => PlayJsString, JsNull, JsNumber => PlayJsNumber, JsBoolean => PlayJsBoolean, JsArray => PlayJsArray}

object ProtoTransformer {

  def toProto(flowId: String, batchInfo: ModelBatchInfo): BatchInfo = {
    services.events.BatchInfo(flowId, infosToproto(batchInfo.data))
  }

  private def infosToproto(infos: Seq[ModelInfo]): Seq[Info] = {
    infos.map(info => services.events.Info(info.id match {
      case Some(i) => i.value.toString
    }, info.name, Some(toInternalJsObject(info.data)), info.meta ))
  }

  private def toInternalJsObject(jsObject: PlayJsonObject): JsObject = {
    val internalValues = jsObject.value.map { case (label, value) => JsObjectEntry(label, Some(toInternalJsValue(value)))}
    JsObject(internalValues.toSeq)
  }

  private def toInternalJsValue(jsValue: PlayJsValue): JsValue = jsValue match {
    case JsNull => JsValue().withValue(JsValue.Value.Empty)
    case PlayJsString(s) => JsValue().withJsString(JsString(s))
    case PlayJsNumber(n) => JsValue().withJsNumber(JsNumber(Some(toInternalBigDecimal(n))))
    case PlayJsBoolean(bool) => JsValue().withJsBoolean(JsBoolean(bool))
    case PlayJsArray(entries) => JsValue().withJsArray(JsArray(entries.map(toInternalJsValue)))
    case obj: PlayJsonObject => JsValue().withJsObject(toInternalJsObject(obj))
  }

  private def toInternalBigDecimal(value: scala.BigDecimal): BigDecimal = {
    BigDecimal(
      scale = value.scale,
      intVal = Some(BigInteger(
        value = ByteString.copyFrom(value.underlying.unscaledValue.toByteArray)
      ))
    )
  }
}
