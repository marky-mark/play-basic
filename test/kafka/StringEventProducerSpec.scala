package kafka

import _root_.utils.KafkaProvided
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import services.events.{InternalProducerConfig, StringEventProducer}

import scala.collection.JavaConverters._


class StringEventProducerSpec extends FlatSpec
  with Matchers
  with ScalaFutures
  with KafkaProvided
  with BeforeAndAfterAll {

  implicit val actorSystem = ActorSystem("kafka-integration-test")
  implicit val materializer = ActorMaterializer()
  implicit val ec = actorSystem.dispatcher

  val producerConfig = InternalProducerConfig.stringProducer(config)

  override val inboundTopic = producerConfig.topic

  override def beforeAll() = setupKafka()
  override def afterAll() = teardownKafka()

  "StringEventProducer" should "produce a single event" in {
    lazy val producer = new StringEventProducer(producerConfig)

    val key = "partition-key"
    val event = "dummy-event"

    producer.send(key, event).futureValue should === (())
    val result = kafka.retryingReadMessages(inboundTopic, 1).asScala
    result contains event shouldBe true
    result.size shouldBe 1
  }

}
