package services.events

import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, FiniteDuration}

case class KafkaConsumerConfig(groupId: String, topic: String, groupingSize: Int,
                               bootstrapServer: String, groupingDuration: FiniteDuration)

object ConsumerConfig {
  def apply(config: Config): KafkaConsumerConfig =
    KafkaConsumerConfig(config.getString("kafka.consumer.group-id"),
      config.getString("kafka.consumer.topic"),
      config.getInt("kafka.consumer.grouping-size"),
      config.getString("kafka.bootstrap.servers"),
      Duration.fromNanos(config.getDuration("kafka.consumer.grouping-duration").toNanos))
}