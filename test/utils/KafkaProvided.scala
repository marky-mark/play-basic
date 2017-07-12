package utils

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import info.batey.kafka.unit.KafkaUnit
import org.apache.kafka.common.errors.TopicExistsException

trait KafkaProvided extends LazyLogging {
  val config = ConfigFactory.load("application.test.conf")

  val zkPort = config.getString("kafka.zookeeper.servers").split(":")(1).toInt
  val kafkaPort = config.getString("kafka.bootstrap.servers").split(":")(1).toInt
  val inboundTopic = config.getString("kafka.internal-infos-events.topic")

  val kafka = new KafkaUnit(zkPort, kafkaPort)

  def setupKafka() = {

    kafka.startup()
    try {
      kafka.createTopic(inboundTopic)
    }
    catch {
      case e: TopicExistsException => logger.error("topic already exists") //soz
    }
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
      retrying(30)(self.readMessages(topicName, expectedMessages))
    }
  }

}