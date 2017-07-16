package services.events

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class EventConsumer(consumerConfig: KafkaConsumerConfig) extends LazyLogging {

  def run()(implicit ec: ExecutionContext, actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) =  Future[Unit] {
    logger.info("Starting consumer")
    Future.successful(())
  }

}
