package services.events

import com.typesafe.config.Config
import org.apache.kafka.common.serialization.{Serializer, StringSerializer}
import org.apache.kafka.clients.producer.{ProducerConfig => KafkaProducerConfig}

trait ProducerConfig {

  def topic: String
  def props: java.util.Properties
}

class ProducerConfigImpl(name: String) {

  def stringProducer(config: Config): ProducerConfig =
    apply(config, name, new StringSerializer)

  def apply[T](configuration: Config, name: String, serializer: Serializer[T]): ProducerConfig = {

    val kafkaConfig = configuration.getConfig("kafka")
    val producerConfig = kafkaConfig.getConfig(name)

    new ProducerConfig {
      override val topic = producerConfig.getString("topic")

      override val props = {
        val p = new java.util.Properties()
        p.put(KafkaProducerConfig.RETRIES_CONFIG, "0")
        p.put(KafkaProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getString("bootstrap.servers"))
        p.put(KafkaProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaConfig.getString("producer.request.size"))
        p.put(KafkaProducerConfig.KEY_SERIALIZER_CLASS_CONFIG , classOf[StringSerializer])
        p.put(KafkaProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer.getClass)
        p
      }

      val underlying = configuration
    }
  }
}

object InternalProducerConfig extends ProducerConfigImpl("internal-infos-events")