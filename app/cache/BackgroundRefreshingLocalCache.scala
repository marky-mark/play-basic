package cache

import com.google.common.cache.{Cache, CacheBuilder}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.Success

/**
  * A background refreshing cache that periodically calls a provided function to provide refresh data.
  * This cache allows for new data to be added between refreshes (memoisation).
  *
  * @param desc
  * @param refreshPeriod
  * @param maximumSize
  * @param refreshData Function that returns access to data to refresh the cache. It is called with the existing
  *                    keys in the cache at the time of calling.
  * @param ec
  * @tparam K
  * @tparam V
  */
class BackgroundRefreshingLocalCache[K <: AnyRef, V <: AnyRef](desc: String, refreshPeriod: FiniteDuration, maximumSize: Option[Long] = None)
                                                              (refreshData: Set[K] => Future[Seq[(K, V)]])
                                                              (implicit ec: ExecutionContext) extends LazyLogging {

  private var stopped = true

  private val cache: Cache[K, V] = {
    val unsized = CacheBuilder.newBuilder()
    maximumSize.fold(unsized)(unsized.maximumSize).build[K, V]
  }

  def start(maxWait: FiniteDuration): Unit = {
    val populated = Promise[Unit]
    stopped = false
    FutureUtils.repeatWhile[Unit](_ => !stopped, backoff = refreshPeriod, timeout = 6236.days) {
      populate().andThen {
        case Success(_) => populated.trySuccess(())
      }
    }
    Await.result(populated.future, maxWait)
  }

  def stop(): Unit = {
    stopped = true
  }

  private def populate(): Future[Unit] = {
    FutureUtils.timed(millis => logger.info(s"population of '$desc' cache took $millis milliseconds")) {
      val existingKeys = cache.asMap().keySet().asScala.toSet
      refreshData(existingKeys).map { case newData =>
        val newDataMap = newData.toMap
        val removedKeys = existingKeys diff newDataMap.keySet
        cache.putAll(newDataMap.asJava)
        cache.invalidateAll(removedKeys.asJava)
      }.recover { case ex =>
        logger.error(s"Failed to (re)populate background-refreshing cache '$desc'", ex)
      }
    }
  }

  /**
    * While this cache is background populated/refreshed, there is still a chance that new data is available
    * outside this cycle - it should be added here.
    */
  def memo(key: K)(ifNotFound: K => Future[V])(implicit ec: ExecutionContext): Future[V] = {
    Option(cache.getIfPresent(key)) match {
      case Some(e) => Future.successful(e)
      case None => ifNotFound(key).andThen {
        case Success(el) => cache.put(key, el)
      }
    }
  }
}
