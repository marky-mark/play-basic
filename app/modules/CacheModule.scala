package modules

import akka.actor.ActorSystem
import cache.{CachingContext, CircuitBreaker}
import com.typesafe.play.redis.{SedisPoolProvider, RedisCacheApi}
import play.api.cache.{CacheApi, EhCacheComponents}
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment}

import scala.concurrent.duration._
import cache.CircuitBreaker

trait CacheModule { self: ExecutionContexts with MetricsModule =>

  def configuration: Configuration
  def environment: Environment
  def applicationLifecycle: ApplicationLifecycle
  def actorSystem: ActorSystem

  lazy val cache: CacheApi =
    if (configuration.getBoolean("ehcachedisabled").contains(true)) redisCache else ehCache

  lazy val cachingContext = {
    val scheduler = ActorSystem(
      "cache-circuit-breaker",
      defaultExecutionContext = Some(
        fixedSizeExecutionContext("cache-circuit-breaker", 10))
    ).scheduler
    val circuitBreaker = CircuitBreaker(
      name = "Redis cache CB",
      scheduler = scheduler,
      maxFailures = 10,
      callTimeout = 10.millis,
      resetTimeout = 1.second
    )
    CachingContext(CacheModule.this.executionContext("caching-context"), cache, 10.minutes, circuitBreaker, metricService)
  }

  def ehCache: CacheApi = {
    new EhCacheComponents {
      override def applicationLifecycle: ApplicationLifecycle = CacheModule.this.applicationLifecycle
      override def environment: Environment = CacheModule.this.environment
      override def configuration: Configuration = CacheModule.this.configuration
    }.defaultCacheApi
  }

  def redisCache: RedisCacheApi = {
    val jedisPool = new SedisPoolProvider(configuration, applicationLifecycle)
    new RedisCacheApi("play", jedisPool.get, environment.classLoader)
  }
}
