package services.slickbacked

import java.sql.BatchUpdateException
import java.util.UUID

import com.markland.service.tags.ids.InfoId
import com.typesafe.scalalogging.LazyLogging
import metrics.MetricsService
import models.QueryParams
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Json
import services.slickbacked.DBHelpers._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Try}
import scalaz.\/

trait InfoRepository {

  def query(queryParams: QueryParams, limit: Int, startAfter: Option[InfoId])(implicit ec: ExecutionContext): Future[Seq[InfoSlick]]

  def list(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Seq[InfoSlick]]

  def retrieve(salesChannelId: UUID, infoId: UUID)(implicit ec: ExecutionContext): Future[Option[InfoSlick]]

  def insert(info: InfoSlick)(implicit ec: ExecutionContext): Future[Either[String, UUID]] //just doing an Either for fun

  def update(info: InfoSlick)(implicit ec: ExecutionContext): Future[Option[UUID]]

  def batchUpdate(info: Seq[InfoSlick])(implicit ec: ExecutionContext): Future[Unit]

  def batchInsert(infos: Seq[InfoSlick])(implicit ec: ExecutionContext): Future[Option[Int]]

  def getLastModifiedDate(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Option[DateTime]]
}

class InfoRepositoryImpl(dbProvider: DatabaseProvider, metricsService: MetricsService) extends InfoRepository with LazyLogging {

  private val db = dbProvider.database
  private val dm = dbProvider.dataModel

  import dm.driver.api._

  override def list(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Seq[InfoSlick]] = metricsService.measureAndIncrementFut("inforepo.list", "inforepo.list") {
    val info = for {
      i <- dm.info if i.salesChannelId === salesChannelId
    } yield i

    db.run(info.result)
  }

  override def insert(info: InfoSlick)(implicit ec: ExecutionContext): Future[Either[String, UUID]] = {

    val insertInfo = (dm.info returning dm.info.map(_.id) into ((item, id) => item.copy(id = id))) += info

    val action = for {
      i <- insertInfo
    } yield Right(i.id)

    db.run(action.transactionally).recover {
      case e: Exception => logger.error("Error caught", e); Left("Exception Occured")
    }
  }

  override def update(info: InfoSlick)(implicit ec: ExecutionContext): Future[Option[UUID]] = {
    db.run(dm.info.filter(_.salesChannelId === info.salesChannelId)
      .filter(_.id === info.id)
      .map(i => (i.name, i.data, i.meta, i.lastModified))
      .update((info.name, Json.toJson(info.data), info.meta, info.lastModified))).map {
      case 0 => None
      case _ => Some(info.id)
    }
  }

  //sadly slick cannot do batch upserts :(

  override def batchInsert(infos: Seq[InfoSlick])(implicit ec: ExecutionContext): Future[Option[Int]] = {

    import services.slickbacked.FutureUtils._

    db.run {
      dm.info ++= infos
    }.logOnFailure(ex => s"Failed to create a batch of info: $ex")
  }

  override def batchUpdate(info: Seq[InfoSlick])(implicit ec: ExecutionContext): Future[Unit] = {

    val batchStmt = SimpleDBIO[Unit] { ctx =>
      closing(ctx.connection.prepareStatement(
        "update info set name = ?, data = ?, meta = ?, sales_channel_id = ?, last_modified = ?, status = ? where id = ?")) { pstmt =>

        info.foreach { i =>

          pstmt.setString(1, i.name)
          pstmt.setObject(2, asPGObject(Json.toJson(i.data)))
          pstmt.setArray(3, ctx.connection.createArrayOf("text", i.meta.toArray))
          pstmt.setObject(4, i.salesChannelId)
          pstmt.setTimestamp(5, i.lastModified)
          pstmt.setString(6, i.status)
          pstmt.setObject(7, i.id)
          pstmt.addBatch()
        }
        pstmt.executeBatch()
      }
      ()
    }

    import services.slickbacked.FutureUtils._

    db.run(batchStmt).logOnFailure(ex => s"Failure interacting with DB while updating infos: $ex")
  }

  override def getLastModifiedDate(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Option[DateTime]] = {
    db.run(dm.info.filter(_.salesChannelId === salesChannelId).map(_.lastModified).max.result)
      .map(o => o.map(t => new DateTime(t.getTime).toDateTime(DateTimeZone.UTC)))
  }

  override def retrieve(salesChannelId: UUID, infoId: UUID)(implicit ec: ExecutionContext): Future[Option[InfoSlick]] = {
    val info = for {
      i <- dm.info if i.salesChannelId === salesChannelId && i.id === infoId
    } yield i

    db.run(info.result.headOption)
  }

  override def query(queryParams: QueryParams, limit: Int, startAfter: Option[InfoId])(implicit ec: ExecutionContext): Future[Seq[InfoSlick]] = {
    val filteredQuery = {
      val base = queryParams.salesChannelId match {
        case Some(ownerId) => dm.info.filter(_.salesChannelId === ownerId.value)
        case None => dm.info
      }

      //can do more filters if you you like

      base
    }

    val baseQuery = filteredQuery.sortBy(_.id)
    val infoQuery = startAfter.map(safter => baseQuery.filter(_.id > safter.value)).getOrElse(baseQuery).take(limit)

    import services.slickbacked.FutureUtils._

    db.run {
      {
        infoQuery.result
      }
    }.logOnFailure(ex => s"Failure interacting with DB while querying Products: $ex")
  }
}

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
