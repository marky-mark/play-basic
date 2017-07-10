package services.events

import com.google.protobuf.ByteString
import com.markland.service.models.{BatchInfo => ModelBatchInfo, Info => ModelInfo, InfoStatusEnum => ModelInfoStatusEnum}
import com.markland.service.tags.ids._
import play.api.libs.json.{JsNull, JsArray => PlayJsArray, JsBoolean => PlayJsBoolean, JsNumber => PlayJsNumber, JsObject => PlayJsonObject, JsString => PlayJsString, JsValue => PlayJsValue}

import scalaz.\/
import scalaz.syntax.either._

object ProtoTransformer {

  def toProto(flowId: Option[FlowId], batchInfo: ModelBatchInfo): BatchInfo = {
    services.events.BatchInfo(flowId.map(_.value).getOrElse(""), infosToproto(batchInfo.data))
  }

  private def infosToproto(infos: Seq[ModelInfo]): Seq[Info] = {
    infos.flatMap(info => info.id.map(i =>
      services.events.Info(i.value.toString, info.name, Some(toInternalJsObject(info.data)), info.meta,
        toInternalProductStatus(info.status).getOrElse(InfoStatus.ACTIVE) )))
  }

  private def toInternalProductStatus(status: ModelInfoStatusEnum.InfoStatus): String \/ InfoStatus = status match {
    case ModelInfoStatusEnum.Active => InfoStatus.ACTIVE.right
    case ModelInfoStatusEnum.Inactive => InfoStatus.INACTIVE.right
    case _ => s"Invalid Internal Info Status: $status, can't encode".left
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
