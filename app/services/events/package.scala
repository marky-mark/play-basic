package services

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

import scala.concurrent.Future

package object events {

  implicit class KafkaProducerOps[K, V](val producer: KafkaProducer[K, V]) extends AnyVal {
    def sendAsync(msg: ProducerRecord[K, V]): Future[RecordMetadata] = {
      val callback = new FutureCompletingCallback()
      producer.send(msg, callback)
      callback.future
    }
  }

}
