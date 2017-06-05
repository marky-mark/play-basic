package cache

import java.sql.BatchUpdateException
import java.util.concurrent.{Executors, TimeUnit}

import akka.pattern.{CircuitBreaker => AkkaCircuitBreaker}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.TraversableOnce
import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.{Deadline, Duration, DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.higherKinds
import scala.util.control.NonFatal
import scala.util.{Failure, Random, Try}
import scalaz.\/
import scalaz.syntax.either._


object FutureUtils extends LazyLogging {
  private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

  sealed trait Completion[+T]

  case object Abandoned extends Completion[Nothing]
  case class Completed[T](t: T) extends Completion[T]

  implicit class FutureCompletionSupport[T](val fut: Future[Completion[T]]) extends AnyVal {
    def resolve[R](onAbandon: => Future[R])(onSuccess: T => Future[R])(implicit ec: ExecutionContext): Future[R] = {
      fut.flatMap {
        case Abandoned => onAbandon
        case Completed(t) => onSuccess(t)
      }
    }
  }

  /**
    * Equivalent to `Seq.foldLeft`, but where the operation returns a `Future`. Each `Future` here is evaluated serially -
    * i.e. one future is fully completed prior to the next being started.
    * First failing future will result in a failed future being returned here.
    */
  def seriallyFoldLeft[A, B, M[X] <: TraversableOnce[X]](input: M[A])(z: B)(op: (B, A) => Future[B])(implicit ec: ExecutionContext): Future[B] = {
    def recur(remaining: Iterator[A], acc: B): Future[B] = {
      if (remaining.hasNext) {
        op(acc, remaining.next()).flatMap(recur(remaining, _))
      } else {
        Future.successful(acc)
      }
    }

    recur(input.toIterator, z)
  }

  def serially[A, B, M[X] <: TraversableOnce[X]](in: M[A])(fn: A => Future[B])(implicit cbf: CanBuildFrom[M[A], B, M[B]], ec: ExecutionContext): Future[M[B]] = {
    seriallyFoldLeft(in)(cbf(in)) { case (acc, a) => fn(a).map(acc += _) }.map(_.result())
  }

  /**
    * Similar to `Future.traverse`, but not caring about the result of the `Future`, but rather, only concerned about their completion.
    *
    * There is no serial guarantees in this function, rather, multiple futures are started concurrently.
    *
    * Any failed future will result in a failed future being returned here.
    */
  def afterAll[T, R](input: Seq[T])(fn: T => Future[R])(implicit ec: ExecutionContext): Future[Unit] = {
    input.foldLeft(Future.successful(())) { case (acc, tee) => fn(tee).flatMap(_ => acc) }
  }

  /**
    * Similar to `serially` but without the overhead of collecting the results of each future - useful when the results are
    * being discarded.
    */
  def seriallyForeach[A, B, M[X] <: TraversableOnce[X]](in: M[A])(fn: A => Future[B])(implicit ec: ExecutionContext): Future[Unit] = {
    seriallyFoldLeft(in)(()) { case (_, a) => fn(a).map(_ => ()) }
  }

  /**
    * Similar functionality as `Seq.collectFirst`, however, here the result of the `PartialFunction` is a `Future`
    */
  def seriallyCollectFirst[A, B, M[X] <: TraversableOnce[X]](input: M[A])(pf: PartialFunction[A, Future[B]])(implicit ec: ExecutionContext): Future[Option[B]] = {
    def recur(remaining: Iterator[A]): Future[Option[B]] = {
      if (remaining.hasNext) {
        val head = remaining.next()
        if (pf.isDefinedAt(head)) {
          pf(head).map(Some(_))
        } else {
          recur(remaining)
        }
      } else {
        Future.successful(None)
      }
    }
    recur(input.toIterator)
  }

  /**
    * This is like `serially`, concatenating on the right side of a disjunction.
    * Additionally, it will fail on the first operation that returns a `-\/` (i.e. left)
    */
  def seriallyTraverseU[A, B, E, M[X] <: TraversableOnce[X]](input: M[A])(fn: A => Future[\/[E, B]])(implicit cbf: CanBuildFrom[M[A], B, M[B]], ec: ExecutionContext): Future[\/[E, M[B]]] = {
    def recur(remaining: Iterator[A], acc: \/[E, scala.collection.mutable.Builder[B, M[B]]]): Future[\/[E, scala.collection.mutable.Builder[B, M[B]]]] = {
      if (remaining.hasNext) {
        fn(remaining.next()).flatMap(disj => {
          disj.fold(l => Future.successful(l.left), r => recur(remaining, acc.map(builder =>  builder += r)))
        })
      } else {
        Future.successful(acc)
      }
    }

    recur(input.toIterator, cbf().right[E]).map(_.map(_.result()))
  }

  /**
    * Repeatedly execute a body while a predicate holds on the result of a single body.
    *
    * @return
    */
  def repeatWhile[T](pred: T => Boolean,
                     backoff: Duration = 50.millis,
                     timeout: FiniteDuration = 60.seconds)
                    (bod: => Future[T])
                    (implicit ec: ExecutionContext): Future[Completion[T]] = {
    val deadline = Deadline.now + timeout

    def recur(tee: T): Future[Completion[T]] = {
      if (pred(tee)) {
        if (deadline.isOverdue()) {
          Future.successful(Abandoned)
        } else {
          withDelay(backoff)(bod.flatMap(recur))
        }
      } else {
        Future.successful(Completed(tee))
      }
    }
    bod.flatMap(recur)
  }

  /**
   * Time a future's execution.
   *
   * @param report Function that takes a millisecond value.
   */
  def timed[T](report: Long => Unit)(bod: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val startnanos = System.nanoTime
    bod.andThen { case _ =>
      val duration = System.nanoTime - startnanos
      report(duration/1000000)
    }
  }

  def retrying[T](description: String,
                  attempts: Int = Int.MaxValue,
                  delay: Duration = 10.millisecond,
                  maxUnJitteredDelay: Duration = 10.minutes,
                  jitterLimit: Double = 0.1,
                  onAttempt: => Unit = (),
                  deadline: Deadline = Deadline.now + 7.days)
                 (fut: => Future[T])
                 (implicit ec: ExecutionContext): Future[T] = {
    require(jitterLimit <= 1d && jitterLimit >= 0d, "Jitter must be in the range 0-1")

    require(maxUnJitteredDelay < 365.days, "Maximum delay must be less than 1 year")
    require(delay <= maxUnJitteredDelay, "Specified Delay must not be more than the maximum delay")

    // Generates a jitter multiplier from a range of (1.0 - jitter, 1.0 + jitter)
    def randomJitter = 1.0 + jitterLimit * (2 * Random.nextDouble() - 1)

    safe(fut).recoverWith {
      case NonFatal(ex) if attempts > 0 && deadline.hasTimeLeft =>
        val actualDelay = delay * randomJitter
        logger.warn(s"Failed $description - retrying in $actualDelay", ex)
        onAttempt
        withDelay(actualDelay)(retrying(description, attempts - 1, (delay * 2) min maxUnJitteredDelay, maxUnJitteredDelay, jitterLimit, onAttempt, deadline = deadline)(fut))
    }
  }


  /**
    * Convert a body returning a Future (that could itself throw an exception) into a Failed future if it does throw an exception.
    */
  def safe[T](fut: => Future[T]): Future[T] = {
    Try(fut).recover { case ex => Future.failed[T](ex) }.get
  }

  def withDelay[T](delay: Duration)(bod: => Future[T]): Future[T] = {
    try {
      val promise = Promise[T]
      scheduledExecutor.schedule(
        new Runnable() {
          def run() = promise.completeWith(bod)
        },
        delay.toMillis, TimeUnit.MILLISECONDS
      )
      promise.future
    } catch {
      case ex: Throwable =>
        logger.error(s"Failed to schedule body during 'withDelay'", ex)
        Future.failed(ex)
    }
  }

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

  implicit class LazyFutureUtilsSupport[T](self: => Future[T]) {
    def delayed(delay: Duration): Future[T] = withDelay(delay)(self)
    def timed(report: Long => Unit)(implicit ec: ExecutionContext): Future[T] = FutureUtils.timed(report)(self)
    def safe: Future[T] = FutureUtils.safe(self)


    def repeatWhile(backoff: Duration = 50.millis,
                    timeout: FiniteDuration = 60.seconds)
                   (pred: T => Boolean)
                   (implicit ec: ExecutionContext): Future[Completion[T]] = {
      FutureUtils.repeatWhile(pred, backoff, timeout)(self)
    }

    def retrying(description: String,
                 attempts: Int = Int.MaxValue,
                 delay: Duration = 10.millisecond,
                 maxUnJitteredDelay: Duration = 10.minutes,
                 jitterLimit: Double = 0.1,
                 onAttempt: => Unit = (),
                 deadline: Deadline = Deadline.now + 7.days)
                (implicit ec: ExecutionContext): Future[T] = {
      FutureUtils.retrying(description, attempts, delay, maxUnJitteredDelay, jitterLimit, onAttempt, deadline)(self)
    }

    def retryingInCircuit(circuitBreaker: AkkaCircuitBreaker,
                          description: String,
                          delay: Duration = 100.milliseconds,
                          deadline: Deadline = Deadline.now + 7.days,
                          maxUnJitteredDelay: Duration = 10.seconds)
                         (implicit ec: ExecutionContext): Future[T] = {
      FutureUtils.retrying(description, delay = delay, maxUnJitteredDelay = maxUnJitteredDelay, deadline = deadline) {
        circuitBreaker.withCircuitBreaker(self)
      }
    }
  }
}
