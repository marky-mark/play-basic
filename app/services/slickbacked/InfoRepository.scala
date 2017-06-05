package services.slickbacked

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import metrics.MetricsService
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

trait InfoRepository {
  def list(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Seq[InfoSlick]]

  def insert(info: InfoSlick)(implicit ec: ExecutionContext): Future[Either[String,UUID]] //just doing an Either for fun

  def update(info: InfoSlick)(implicit ec: ExecutionContext): Future[Option[UUID]]

  def getLastModifiedDate(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Option[DateTime]]
}

class InfoRepositoryImpl(dbProvider: DatabaseProvider, metricsService: MetricsService) extends InfoRepository with LazyLogging {

  private val db = dbProvider.database
  private val dm = dbProvider.dataModel

  import dm.driver.api._

  override def list(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Seq[InfoSlick]] =  metricsService.measureAndIncrementFut("inforepo.list", "inforepo.list") {
    val info = for {
      i <- dm.info if i.salesChannelId === salesChannelId
    } yield i

    db.run(info.result)
  }

  override def insert(info: InfoSlick)(implicit ec: ExecutionContext): Future[Either[String,UUID]] = {

    val insertInfo = (dm.info returning dm.info.map(_.id) into ((item, id) => item.copy(id = id))) += info

    val action = for {
      i <- insertInfo
    } yield Right(i.id)

    db.run (action.transactionally).recover {
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

  override def getLastModifiedDate(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Option[DateTime]] = {
    db.run(dm.info.filter(_.salesChannelId === salesChannelId).map(_.lastModified).max.result)
      .map(o => o.map(t => new DateTime(t.getTime).toDateTime(DateTimeZone.UTC)))
  }
}
