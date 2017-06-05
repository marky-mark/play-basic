package cache

import com.google.common.cache.{Cache, CacheBuilder}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

private[cache] class LocalCache[E <: AnyRef](ttl: FiniteDuration, maximumSize: Int) {
  private val cache: Cache[AnyRef, E] = CacheBuilder.newBuilder()
    .maximumSize(maximumSize.toLong)
    .expireAfterWrite(ttl.length, ttl.unit)
    .build[AnyRef, E]

  def memo[ID <: AnyRef](key: ID)(ifNotFound: ID => Future[E])(implicit ec: ExecutionContext): Future[E] = {
    Option(cache.getIfPresent(key)) match {
      case Some(e) => Future.successful(e)
      case None => ifNotFound(key).andThen {
        case Success(el) => cache.put(key, el)
      }
    }
  }

  def invalidate[ID <: AnyRef](key: ID) = cache.invalidate(key)
}

object LocalCache {
  def apply[E <: AnyRef](ttl: FiniteDuration, maximumSize: Int) = new LocalCache[E](ttl, maximumSize)
}

