package services.events

import org.apache.kafka.clients.producer.{Callback, RecordMetadata}

import scala.concurrent.{Future, Promise}

private class FutureCompletingCallback extends Callback {
  private val promise = Promise[RecordMetadata]

  def future: Future[RecordMetadata] = promise.future

  override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
    Option(metadata) match {
      case Some(_) => promise.success(metadata)
      case _       => promise.failure(exception)
    }
  }
}
