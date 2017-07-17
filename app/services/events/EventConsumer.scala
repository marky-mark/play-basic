package services.events

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig => KafkaConsumerConfig}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import play.api.Logger
import services.InfoService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class EventConsumer(internalConsumerConfig: InternalKafkaConsumerConfig, infoService: InfoService)
                   (implicit ec: ExecutionContext, actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) extends LazyLogging {

  val consumerSettings =
    ConsumerSettings(actorSystem,
      new StringDeserializer,
      new ByteArrayDeserializer)
      .withGroupId(internalConsumerConfig.groupId)
      .withBootstrapServers(internalConsumerConfig.bootstrapServer)
      .withProperty(KafkaConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def run(): Future[Done] = {
    logger.info("Starting consumer")

    Consumer.committableSource(consumerSettings, Subscriptions.topics(internalConsumerConfig.topic))
      .mapAsync(internalConsumerConfig.concurrency)(handleEvent)
      .map(il => il.map(i => logger.info(s"Info ${i}" )))
      .runWith(Sink.ignore)
  }

  private def handleEvent(commitableMessage: CommittableMessage[String, Array[Byte]]) = Future[List[Internalevent.Info]] {
    val batchInfos: Option[Internalevent.BatchInfo] = safelyFromBytes(commitableMessage.record.value())

    import collection.JavaConverters._

    batchInfos match {
      case Some(b) => b.getInfoList.asScala.toList
      case None => List()
    }

  }

  private def safelyFromBytes[T](data: Array[Byte]): Option[Internalevent.BatchInfo] = {
    Try {
      Internalevent.BatchInfo.parseFrom(data)
    }.map { i =>
      logger.info(s"Successfully parsed batch for flow id ${i.getFlowId} and ${i.getInfoList}")
      Some(i)
    }.recover {
      case NonFatal(err) =>
        Logger.error(s"Complete failure to parse bytes as Batch Info:", err)
        None
    }.get
  }

}
