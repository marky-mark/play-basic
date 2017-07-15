package services.events

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

class EventConsumer extends LazyLogging {

  def run()(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) = {
    logger.info("Starting consumer")
  }

}
