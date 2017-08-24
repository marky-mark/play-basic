package services.slickbacked

import java.util.UUID

import akka.actor.ActorSystem
import cache.{CachingContext, CircuitBreaker}
import metrics.MetricsService
import net.sf.ehcache.CacheManager
import play.api.cache.EhCacheApi

import scala.concurrent.duration.DurationInt

object EhCacheProvided {
  val cacheName = UUID.randomUUID().toString
  val cacheManager = new CacheManager()
  cacheManager.addCache(cacheName)

  val cacheApi = new EhCacheApi(cacheManager.getCache(cacheName))
}

trait EhCacheProvided {
  def metricsService: MetricsService
  def cachingContext: CachingContext = {
    val scheduler = ActorSystem("cache-circuit-breaker").scheduler

    val circuitBreaker = CircuitBreaker(
      name = "Redis cache CB",
      scheduler = scheduler,
      maxFailures = 10,
      callTimeout = 10.millis,
      resetTimeout = 1.second)
    CachingContext(scala.concurrent.ExecutionContext.Implicits.global, EhCacheProvided.cacheApi, 0.nanosecond, circuitBreaker, metricsService)
  }
}