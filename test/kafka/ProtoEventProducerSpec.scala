package kafka

import _root_.utils.KafkaProvided
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import services.events._

import scala.collection.JavaConverters._
import scala.collection.mutable


class ProtoEventProducerSpec extends FlatSpec
  with Matchers
  with ScalaFutures
  with KafkaProvided
  with BeforeAndAfterAll {

  implicit val actorSystem = ActorSystem("kafka-integration-test")
  implicit val materializer = ActorMaterializer()
  implicit val ec = actorSystem.dispatcher

  val producerConfig = InternalProducerConfig.protoProducer(config)

  override val inboundTopic = producerConfig.topic

  override def beforeAll() = setupKafka()
  override def afterAll() = teardownKafka()

  "ProtoEventProducer" should "produce a single event" in {

    lazy val producer = new ProtoEventProducer(producerConfig)

    val key = "partition-key"
    val event: BatchInfo = BatchInfo("foo", Seq(Info("bar", "name", None, Seq("one")) ) )

    producer.send(key, event.toByteArray).futureValue should === (())
    val result: mutable.Buffer[String] = kafka.retryingReadMessages(inboundTopic, 1).asScala
    val consumed = result.map(_.toCharArray.map(_.toByte)).map(BatchInfo.parseFrom)
    consumed.head should === (event)
  }

}
