package kafka

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import _root_.utils.KafkaProvided
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
  lazy val producer = new StringEventProducer(producerConfig)
  override val inboundTopic = producerConfig.topic

  override def beforeAll() = setupKafka()
  override def afterAll() = teardownKafka()

  "StatusEventProducer" should "produce a single event" in {
    val event = "dummy-event"
    val key = "partition-key"

    producer.send(key, event).futureValue should === (())
    val result = kafka.retryingReadMessages(inboundTopic, 1).asScala
    result contains event shouldBe true
    result.size shouldBe 1
  }

}
