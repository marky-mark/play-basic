package utils

import com.typesafe.config.ConfigFactory
import info.batey.kafka.unit.KafkaUnit
import setup.BaseSetup

trait KafkaProvided extends BaseSetup {
  val config = ConfigFactory.load()

  val zkPort = config.getString("kafka.zookeeper.servers").split(":")(1).toInt
  val kafkaPort = config.getString("kafka.bootstrap.servers").split(":")(1).toInt
  val inboundTopic = config.getString("kafka.outbound-events.topic")

  val kafka = new KafkaUnit(zkPort, kafkaPort)

  def setupKafka() = {
    kafka.startup()
    kafka.createTopic(inboundTopic)
  }

  def teardownKafka() = {
    kafka.shutdown()
  }

  implicit class KafkaUnitOps(val self: KafkaUnit) {
    /**
      * Due to KafkaUnit internally hardcoding the read timeout, we wrap here with a retry in an attempt to
      * allow more time to get messages... sigh...
      */
    def retryingReadMessages(topicName: String, expectedMessages: Int): java.util.List[String] = {
      def retrying[T](maxCount: Int)(fn: => T): T = {
        def recur(retryCount: Int): T = {
          try fn
          catch {
            case _ if retryCount < maxCount => recur(retryCount + 1)
          }
        }
        recur(1)
      }
      retrying(20)(self.readMessages(topicName, expectedMessages))
    }
  }

}