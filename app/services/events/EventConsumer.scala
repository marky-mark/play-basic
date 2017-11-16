package services.events

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Flow}
import com.markland.service.models.{BatchInfo => InternalBatchInfo, Info => InternalInfo, BatchInfoUpdateStatusResultEnum, BatchInfoUpdateStatusStatusEnum}
import com.markland.service.tags.ids._
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig => KafkaConsumerConfig}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import play.api.Logger
import services.InfoService
import services.slickbacked._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class EventConsumer(internalConsumerConfig: InternalKafkaConsumerConfig, infoService: InfoService, eventTrackingRepository: EventTrackingRepository)
                   (implicit ec: ExecutionContext, actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) extends LazyLogging {

  val consumerSettings =
    ConsumerSettings(actorSystem,
      new StringDeserializer,
      new ByteArrayDeserializer)
      .withGroupId(internalConsumerConfig.groupId)
      .withBootstrapServers(internalConsumerConfig.bootstrapServer)
      .withProperty(KafkaConsumerConfig.AUTO_OFFSET_RESET_CONFIG, internalConsumerConfig.autoOffsetRest)

  def logOut = Flow[Option[(SalesChannelId, Seq[InternalInfo], UUID)]]
      .map{ il => logger.info(s"Updating $il"); il}

  def run(): Future[Done] = {

    Consumer.committableSource(consumerSettings, Subscriptions.topics(internalConsumerConfig.topic))
      .log("Starting consumer")
      //      .throttle(1, 1.second, 1, ThrottleMode.shaping)
      .mapAsync(internalConsumerConfig.concurrency)(handleEvent)
      .via(logOut)
      .map(il => il.map { case (sc, i, gid) =>
//        logger.info(s"Updating Sales Channel $sc Info $i")
        infoService.batchUpdate(sc, i).map {
          case Some(num) =>

            logger.info(s"$num inserted")

            num match {
              case 0 => Left(FailedResult.apply(sc, Some(Operation.Insert()), gid, Stopped.updated()))
              case n => Right(SuccessfulResult.apply(sc, gid, ResultType.Inserted, Operation.Insert()))
            }
          case None => Left(FailedResult.apply(sc, Some(Operation.Insert()), gid, Stopped.untrackedInternalError("No Idea")))
        }
      })
      .runForeach(r => r.map( res => {
        logger.info(s"Result is $res ")

        res map {
          case Right(s) => eventTrackingRepository.updateTracking(s.salesChannelId.value, s.groupId, BatchInfoUpdateStatusStatusEnum.Successful,
            Some(BatchInfoUpdateStatusResultEnum.Inserted), None)
          case Left(e) => eventTrackingRepository.updateTracking(e.salesChannelId.value, e.groupId, BatchInfoUpdateStatusStatusEnum.Failed,
            Some(e.stopped.result), Some(e.stopped.problems))
        }
      })
    )
    //      .runWith(Sink.ignore)
  }

  private def handleEvent(commitableMessage: CommittableMessage[String, Array[Byte]]): Future[Option[(SalesChannelId, Seq[InternalInfo], UUID)]] =

    Future[Option[(SalesChannelId, Seq[InternalInfo], UUID)]] {

      val batchInfosproto: Option[BatchInfo] = safelyFromBytes(commitableMessage.record.value())

      batchInfosproto.map { b =>
        val (flowId: Option[FlowId], batchInfo: InternalBatchInfo, salesChannelId: SalesChannelId, gid: UUID)
        = ProtoTransformer.fromProto(b)
        (salesChannelId, batchInfo.data, gid)
      }

    }

  private def safelyFromBytes[T](data: Array[Byte]): Option[BatchInfo] = {
    Try {
      BatchInfo.parseFrom(data)
    }.map { i =>
      logger.info(s"Successfully parsed batch for flow id ${i.flowId} and ${i.info}")
      Some(i)
    }.recover {
      case NonFatal(err) =>
        Logger.error(s"Complete failure to parse bytes as Batch Info:", err)
        None
    }.get
  }

}
