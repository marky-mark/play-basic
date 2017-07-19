package services.events

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.concurrent.{ExecutionContext, Future}


trait EventProducer[T] {

  def kafkaClient: KafkaProducer[String, T]

  protected def produce(topic: String, partitionKey: String, event: T)(implicit ec: ExecutionContext): Future[Unit] =
    kafkaClient
      .sendAsync(new ProducerRecord[String, T](topic, partitionKey, event))
      .map(_ => ())

  def send(partitionKey: String, event: T)(implicit ec: ExecutionContext): Future[Unit]

  protected def shutdown() = kafkaClient.close()
}

class StringEventProducer(producerConfig: ProducerConfig) extends EventProducer[String] {

  override val kafkaClient: KafkaProducer[String, String] =
    new KafkaProducer[String, String](producerConfig.props)

  override def send(partitionKey: String, event: String)(implicit ec: ExecutionContext): Future[Unit] =
    produce(producerConfig.topic, partitionKey, event)
}

class ProtoEventProducer(producerConfig: ProducerConfig) extends EventProducer[Array[Byte]] {
  override val kafkaClient: KafkaProducer[String, Array[Byte]] =
    new KafkaProducer[String, Array[Byte]](producerConfig.props)

  override def send(partitionKey: String, event: Array[Byte])(implicit ec: ExecutionContext): Future[Unit] =
    produce(producerConfig.topic, partitionKey, event)
}