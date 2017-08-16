package services.slickbacked

import java.sql.BatchUpdateException

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Try}

object FutureUtils extends LazyLogging {

  implicit class FutureUtilsSupport[T](val self: Future[T]) extends AnyVal {
    def logOnFailure(report: Throwable => String, reportStackTrace: Boolean = false)(implicit ec: ExecutionContext): Future[T] = self.andThen {
      case Failure(ex: BatchUpdateException) if reportStackTrace => logger.error(report(ex.getNextException), ex)
      case Failure(ex: BatchUpdateException) if !reportStackTrace => logger.error(report(ex.getNextException))
      case Failure(ex) if reportStackTrace => logger.error(report(ex), ex)
      case Failure(ex) if !reportStackTrace => logger.error(report(ex))
    }

    def flattenedAndThen[U](pf: PartialFunction[Try[T], Future[U]])(implicit ec: ExecutionContext) = {
      val p = Promise[T]
      self.onComplete {
        case result if pf.isDefinedAt(result) => pf(result).onComplete { case _ => p.complete(result) }
        case result => p.complete(result)
      }
      p.future
    }
  }

}
