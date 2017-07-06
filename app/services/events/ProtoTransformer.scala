package services.events

object ProtoTransformer {

  def toProto(batchInfo: BatchInfo, flowId: String): services.events.BatchInfo = {
    services.events.BatchInfo(flowId, infosToproto(batchInfo.info))
  }

  private def infosToproto(infos: Seq[Info]): Seq[services.events.Info] = {
    infos.map(i => infoToProto(i))
  }

  private def infoToProto(info: Info): services.events.Info = {
    services.events.Info(info.id, info.name, None, info.meta )
  }

}
