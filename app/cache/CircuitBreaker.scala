package cache

import akka.actor.Scheduler
import akka.pattern.{CircuitBreaker => AkkaCircuitBreaker}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.FiniteDuration

object CircuitBreaker extends LazyLogging {
  def apply(name: String, scheduler: Scheduler, maxFailures: Int, callTimeout: FiniteDuration, resetTimeout: FiniteDuration): AkkaCircuitBreaker = {
    AkkaCircuitBreaker(scheduler = scheduler, maxFailures = maxFailures, callTimeout = callTimeout, resetTimeout = resetTimeout)
      .onClose(logger.info(s"$name circuit closed"))
      .onOpen(logger.warn(s"$name circuit open"))
      .onHalfOpen(logger.info(s"$name circuit half-open"))
  }
}
