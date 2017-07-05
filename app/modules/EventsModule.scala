package modules

import com.softwaremill.macwire._
import com.typesafe.config.Config
import play.api.Configuration
import services.events.{InternalProducerConfig, ProtoEventProducer}

trait EventsModule {

  def configuration: Configuration

  private lazy val producerConfig =  InternalProducerConfig.protoProducer(configuration.underlying)

  lazy val internalEventProducer = wire[ProtoEventProducer]
}
