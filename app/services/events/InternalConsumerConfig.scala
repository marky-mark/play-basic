package services.events

import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, FiniteDuration}

case class InternalKafkaConsumerConfig(groupId: String, topic: String, groupingSize: Int,
                                       bootstrapServer: String, groupingDuration: FiniteDuration, concurrency: Int,
                                       autoOffsetRest: String)

object InternalConsumerConfig {
  def apply(config: Config): InternalKafkaConsumerConfig =
    InternalKafkaConsumerConfig(config.getString("kafka.consumer.group-id"),
      config.getString("kafka.internal-infos-events.topic"),
      config.getInt("kafka.consumer.grouping-size"),
      config.getString("kafka.bootstrap.servers"),
      Duration.fromNanos(config.getDuration("kafka.consumer.grouping-duration").toNanos),
      config.getString("kafka.consumer.concurrency").toInt,
      config.getString("kafka.consumer.auto-offset-reset"))
}