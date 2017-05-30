package metrics

import java.util.concurrent.TimeUnit

import com.codahale.metrics.Gauge
import nl.grons.metrics.scala.DefaultInstrumented
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}


trait MetricsService {

  def measureFut[T](name: String)
                   (call: Future[T])(implicit executionContext: ExecutionContext): Future[T]

  def measure[T](name: String)(func: => T): T

  def incrementFut[T](name: String, n: Long = 1)
                     (call: Future[T])
                     (implicit executionContext: ExecutionContext): Future[T]

  def increment(name: String, n: Long = 1): Unit

  def decrement(name: String, n: Long = 1): Unit

  def markMeter(name: String, value: Long = 1L): Unit

  def measureLatency(name: String, from: DateTime): Unit

  def measureAndIncrementFut[T](counterName: String, timerName: String, n: Long = 1)
                               (call: Future[T])
                               (implicit executionContext: ExecutionContext): Future[T]

  def registerGaugeFunc[T](name: String)(func: => T): Gauge[_]
}

class MetricsServiceImpl(metricsConfig: MetricsConfig) extends MetricsService with DefaultInstrumented {

  val environment = metricsConfig.environment

  private def timeBlock(name: String, duration: Long) = {
    metricRegistry.timer(s"$environment.$name.timer").update(duration, TimeUnit.MILLISECONDS)
  }

  override def markMeter(name: String, value: Long = 1L): Unit = {
    metricRegistry.meter(name).mark(value)
  }

  override def measure[T](name: String)(func: => T): T = {
    val startTime = System.currentTimeMillis()
    val retval = func
    val duration: Long = System.currentTimeMillis() - startTime
    timeBlock(name, duration)
    retval
  }

  override def measureFut[T](name: String)
                            (call: Future[T])(implicit executionContext: ExecutionContext): Future[T] = {
    val startTime = System.currentTimeMillis()
    call.onComplete { _ =>
      val duration: Long = System.currentTimeMillis() - startTime
      timeBlock(name, duration)
    }
    call
  }

  override def incrementFut[T](name: String, n: Long = 1)
                              (call: Future[T])(implicit executionContext: ExecutionContext): Future[T] = {
    val counter = metricRegistry.counter(s"$environment.$name.count")
    call.onComplete { case _ => counter.inc(n) }
    call
  }

  override def increment(name: String, n: Long = 1): Unit = {
    val counter = metricRegistry.counter(s"$environment.$name.count")
    counter.inc(n)
  }

  override def decrement(name: String, n: Long = 1): Unit = {
    val counter = metricRegistry.counter(s"$environment.$name.count")
    counter.dec(n)
  }

  override def measureLatency(name: String, from: DateTime): Unit = {
    metricRegistry.timer(s"$environment.$name.timer").update(DateTime.now().getMillis - from.getMillis, TimeUnit.MILLISECONDS)
  }

  override def measureAndIncrementFut[T](counterName: String, timerName: String, n: Long)
                                        (call: Future[T])(implicit executionContext: ExecutionContext): Future[T] = {
    val counter = metricRegistry.counter(s"$environment.$counterName.count")
    val startTime = System.currentTimeMillis()
    call.onComplete { _ =>
      counter.inc(n)
      val duration: Long = System.currentTimeMillis() - startTime
      timeBlock(timerName, duration)
    }
    call
  }

  override def registerGaugeFunc[T](name: String)(func: => T): Gauge[_] = {
    metricRegistry.remove(s"$environment.$name.gauge")
    metricRegistry.register(s"$environment.$name.gauge", new Gauge[T] {
      override def getValue: T = func
    })
  }
}

