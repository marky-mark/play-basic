package modules


import java.io.IOException
import java.net.ConnectException
import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import play.api.Configuration
import services.events.{ConsumerConfig, EventConsumer, KafkaConsumerConfig}

import scala.util.control.NonFatal
import com.softwaremill.macwire._

trait EventsConsumerModule {

  def actorSystem: ActorSystem
  def configuration: Configuration

  implicit lazy val eventsConsumerActorSystem = ActorSystem("consumer-context",
    configuration.underlying.getConfig("consumer-context"))

  val decider: Supervision.Decider = {
    case _: IOException | _: ConnectException | _: TimeoutException => Supervision.Restart
    case NonFatal(err) =>
      //metrics here please!
      actorSystem.log.error("Unhandled Exception in Stream: ", err)
      Supervision.Stop
  }

  implicit lazy val eventsConsumerMaterializer =
    ActorMaterializer(ActorMaterializerSettings(eventsConsumerActorSystem).withSupervisionStrategy(decider))

  implicit val executionContext = actorSystem.dispatchers.lookup("consumer-context")

  private lazy val consumerConfig: KafkaConsumerConfig =  ConsumerConfig.apply(configuration.underlying)

  lazy val consumer = wire[EventConsumer].run().onFailure { case err => actorSystem.log.error(err, "Exception raised in 'consumer'") }

}
