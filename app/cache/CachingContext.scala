package cache

import com.typesafe.scalalogging.LazyLogging
import metrics.MetricsService

import scala.concurrent._
import play.api.cache.CacheApi

import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.{Failure, Success}
import scalaz.Scalaz._
import scalaz.{-\/, \/, \/-}
import cache.FutureUtils._

trait CachingContext {
  def executionContext: ExecutionContext
  def cacheApi: CacheApi
  def ttl: Duration
  def circuitBreaker: akka.pattern.CircuitBreaker
  def metricsService: MetricsService
}

object CachingContext {
  def apply(ec: ExecutionContext, api: CacheApi, timeToLive: Duration, cb: akka.pattern.CircuitBreaker, ms: MetricsService): CachingContext = new CachingContext {
    override val executionContext: ExecutionContext = ec
    override val circuitBreaker: akka.pattern.CircuitBreaker = cb
    override val cacheApi: CacheApi = api
    override val ttl: Duration = timeToLive
    override val metricsService: MetricsService = ms
  }
}

trait Caching[T] extends LazyLogging {

  def cachingPrefix: String
  def metricsPrefix: String
  def cachingContext: CachingContext

  private val ttl = cachingContext.ttl
  private lazy val cache: CacheApi = cachingContext.cacheApi
  private lazy val ms: MetricsService = cachingContext.metricsService
  private lazy val cb: akka.pattern.CircuitBreaker = cachingContext.circuitBreaker
  private implicit lazy val ec: ExecutionContext = cachingContext.executionContext

  def caching(key: String)(bod: => Future[T])(implicit ct: ClassTag[T]): Future[T] = {
    val ckey = cacheKey(key)
    get(ckey).flatMap {
      case None => bod.andThen {
        case Failure(_) =>
        case Success(tee) => cache.set(ckey, tee, ttl)
      }
      case Some(thing) => Future.successful(thing)
    }.recoverWith {
      case ex =>
        logger.error(s"Error while accessing cache for key $ckey, falling back to non cached operation", ex)
        bod
    }
  }

  /**
    * Only cache the right side of a disjunction, ignoring a failed retrieval, or one returning a left side of a disjunction.
    */
  def cachingRight[L](key: String)(bod: => Future[L \/ T])(implicit ct: ClassTag[T]): Future[L \/ T] = {
    val ckey = cacheKey(key)
    get(ckey).flatMap {
      case None => bod.andThen {
        case Failure(_) | Success(-\/(_)) =>
        case Success(\/-(tee)) => cache.set(ckey, tee, ttl)
      }
      case Some(thing) => Future.successful(thing.right[L])
    }.recoverWith {
      case ex =>
        logger.error(s"Error while accessing cache for key $key, falling back to non cached operation", ex)
        bod
    }
  }

  private def get(ckey: String)(implicit ct: ClassTag[T]): Future[Option[T]] = {
    ms.measureFut[Option[T]](s"external-cache.$metricsPrefix.GET") {
      Future(cache.get[T](ckey))
    }.retryingInCircuit(cb, s"retrieving $ckey from cache", 10.millis, 50.millis.fromNow, 15.millis)
      .andThen {
        case Failure(_) => ms.markMeter(s"external-cache.$metricsPrefix.GET.after-retries.FAILED")
      }
  }

  // FIXME this really needs some unit tests, this function is a little more complex than the other simple ones.
  def cachingMultiRight[L, R](keys: Set[String])
                             (getKey: R => String,
                              toCacheValue: R => T,
                              fromCacheValue: T => Option[R])
                             (fetch: Set[String] => Future[L \/ Seq[R]])
                             (implicit ct: ClassTag[T]): Future[L \/ Seq[R]] = {
    val ckeys = keys.map(cacheKey).toSeq
    val futureFromCache = Future.sequence(ckeys.map(get)).map(_.flatten)

    futureFromCache.flatMap { fromCache =>
      val toFetch = keys.diff(fromCache.flatMap(cached => fromCacheValue(cached).map(getKey).toSeq).toSet)

      val futureFetched = if (toFetch.isEmpty) {
        Future.successful(Nil.right)
      } else {
        fetch(toFetch).andThen {
          case scala.util.Success(\/-(things)) => things.foreach { thing =>
            cache.set(cacheKey(getKey(thing)), toCacheValue(thing), ttl)
          }
        }
      }

      futureFetched.map {
        case e @ -\/(_) => e
        case \/-(fetched) => fromCache.flatMap(cached => fromCacheValue(cached).toSeq).union(fetched).right
      }
    }.recoverWith {
      case ex =>
        logger.error(s"Error while accessing cache for keys $ckeys, falling back to non cached operation", ex)
        fetch(ckeys.toSet)
    }
  }

  // FIXME this really needs some unit tests, this function is a little more complex than the other simple ones.
  def cachingMulti[R](keys: Set[String])
                     (getKey: R => String,
                      toCacheValue: R => T,
                      fromCacheValue: T => Option[R])
                     (fetch: Set[String] => Future[Seq[R]])
                     (implicit ct: ClassTag[T]): Future[Seq[R]] = {
    val ckeys = keys.map(cacheKey).toSeq
    val futureFromCache = Future.sequence(ckeys.map(get)).map(_.flatten)

    futureFromCache.flatMap { fromCache =>
      val toFetch = keys.diff(fromCache.flatMap(cached => fromCacheValue(cached).map(getKey).toSeq).toSet)

      val futureFetched = if (toFetch.isEmpty) {
        Future.successful(Nil)
      } else {
        fetch(toFetch).andThen {
          case scala.util.Success(things) => things.foreach { thing =>
            cache.set(cacheKey(getKey(thing)), toCacheValue(thing), ttl)
          }
        }
      }

      futureFetched.map { fetched =>
        fromCache.flatMap(cached => fromCacheValue(cached).toSeq).union(fetched)
      }
    }.recoverWith {
      case ex =>
        logger.error(s"Error while accessing cache for keys $ckeys, falling back to non cached operation", ex)
        fetch(ckeys.toSet)
    }
  }

  def invalidating[R](key: String)(bod: => Future[R]): Future[R] = {
    Future(cache.remove(cacheKey(key))).flatMap(_ => bod)
  }

  private def cacheKey(key: String) = s"$cachingPrefix:$key"

}

