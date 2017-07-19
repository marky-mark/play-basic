package services.events

import java.util.UUID

import com.google.protobuf.ByteString
import com.markland.service.models.{BatchInfo => ModelBatchInfo, Info => ModelInfo, InfoStatusEnum => ModelInfoStatusEnum}
import com.markland.service.tags.ids._
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{JsNull, JsArray => PlayJsArray, JsBoolean => PlayJsBoolean, JsNumber => PlayJsNumber, JsObject => PlayJsonObject, JsString => PlayJsString, JsValue => PlayJsValue}

import scalaz.\/
import scalaz.syntax.either._
import com.markland.service.Id._
import com.markland.service.refs.{FlowRef, SalesChannelRef}

object ProtoTransformer extends LazyLogging {

  def toProto(flowId: Option[FlowId], batchInfo: ModelBatchInfo, salesChannelId: SalesChannelId): BatchInfo = {
    val toproto: Seq[Info] = infosToproto(batchInfo.data)
    logger.info(s"Converting ${batchInfo.data} to ${toproto}")
    services.events.BatchInfo(flowId.map(_.value).getOrElse(""), toproto, salesChannelId.value.toString)
  }

  def fromProto(batchInfos : BatchInfo): (Option[FlowId], ModelBatchInfo, SalesChannelId) = {
    (Some(batchInfos.flowId.id[FlowRef]), ModelBatchInfo(protosToInfos(batchInfos.info)),
      UUID.fromString(batchInfos.salesChannelId).id[SalesChannelRef])
  }

  private def infosToproto(infos: Seq[ModelInfo]): Seq[Info] = {
    infos.map(info => services.events.Info(info.id.getOrElse(UUID.randomUUID().id).value.toString, info.name, Some(toInternalJsObject(info.data)), info.meta,
        toInternalProductStatus(info.status).getOrElse(InfoStatus.ACTIVE), info.lastModified.map(t => DateTime(t.toDate.getTime)) )) // bad!
  }

  private def protosToInfos(infos: Seq[Info]): Seq[ModelInfo] = {
    infos.map(info => ModelInfo(id = Some(UUID.fromString(info.id).id), name = info.name, data = fromInternalJsObject(info.data.get),
      status = fromInternalProductStatus(info.status).getOrElse(ModelInfoStatusEnum.Active), //bad!
      lastModified = info.lastModified.map(t => new org.joda.time.DateTime(t.time)), meta = info.meta))
  }

  private def toInternalProductStatus(status: ModelInfoStatusEnum.InfoStatus): String \/ InfoStatus = status match {
    case ModelInfoStatusEnum.Active => InfoStatus.ACTIVE.right
    case ModelInfoStatusEnum.Inactive => InfoStatus.INACTIVE.right
    case _ => s"Invalid Internal Info Status: $status, can't encode".left
  }

  private def fromInternalProductStatus(status: InfoStatus): String \/ ModelInfoStatusEnum.InfoStatus = status match {
    case InfoStatus.ACTIVE => ModelInfoStatusEnum.Active.right
    case InfoStatus.INACTIVE => ModelInfoStatusEnum.Inactive.right
    case _ => s"Invalid Internal Product Status: $status".left
  }

  private def toInternalJsObject(jsObject: PlayJsonObject): JsObject = {
    val internalValues = jsObject.value.map { case (label, value) => JsObjectEntry(label, Some(toInternalJsValue(value)))}
    JsObject(internalValues.toSeq)
  }
  private def fromInternalJsObject(jsObject: JsObject): PlayJsonObject = {
    val mappedEntries = jsObject.entries.map(entry => (entry.label, fromInternalJsValue(entry.value.get))) //bad! getOrElse
    PlayJsonObject(mappedEntries)
  }

  private def toInternalJsValue(jsValue: PlayJsValue): JsValue = jsValue match {
    case JsNull => JsValue().withValue(JsValue.Value.Empty)
    case PlayJsString(s) => JsValue().withJsString(JsString(s))
    case PlayJsNumber(n) => JsValue().withJsNumber(JsNumber(Some(toInternalBigDecimal(n))))
    case PlayJsBoolean(bool) => JsValue().withJsBoolean(JsBoolean(bool))
    case PlayJsArray(entries) => JsValue().withJsArray(JsArray(entries.map(toInternalJsValue)))
    case obj: PlayJsonObject => JsValue().withJsObject(toInternalJsObject(obj))
  }

  private def fromInternalJsValue(jsValue: JsValue): PlayJsValue = jsValue.value match { //due to option needed..probably best to conver to return Option[PlayJsValue]???
    case JsValue.Value.Empty => JsNull
    case JsValue.Value.JsBoolean(JsBoolean(bool)) => PlayJsBoolean(bool)
    case JsValue.Value.JsNumber(JsNumber(n)) => PlayJsNumber(fromInternalBigDecimal(n.get)) //bad! getOrElse
    case JsValue.Value.JsString(JsString(str)) => PlayJsString(str)
    case JsValue.Value.JsArray(JsArray(entries)) => PlayJsArray(entries.map(fromInternalJsValue))
    case JsValue.Value.JsObject(jsObject) => fromInternalJsObject(jsObject)
  }

  private def toInternalBigDecimal(value: scala.BigDecimal): BigDecimal = {
    BigDecimal(
      scale = value.scale,
      intVal = Some(BigInteger(
        value = ByteString.copyFrom(value.underlying.unscaledValue.toByteArray)
      ))
    )
  }

  private def fromInternalBigDecimal(message: BigDecimal): scala.BigDecimal = {
    scala.BigDecimal(
      unscaledVal = BigInt(message.intVal.get.value.toByteArray), //bad! getOrElse
      scale = message.scale
    )
  }
}
