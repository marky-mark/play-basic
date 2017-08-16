package services.slickbacked

import java.util.UUID.randomUUID

import com.markland.service.Id._
import com.markland.service.models.EnrichmentUpdateStatusStatusEnum._
import com.markland.service.models.EnrichmentUpdateStatusResultEnum._
import com.markland.service.models.{Problem, UpdateInfos}
import com.markland.service.tags.ids.{BatchUpdateId, RequestGroupId, SalesChannelId}
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import services.slickbacked.FutureUtils._

import scala.concurrent.{ExecutionContext, Future}

trait EventTrackingRepository {
  def createTracking(salesChannelId: SalesChannelId,
                     groupId: Option[RequestGroupId])(implicit ec: ExecutionContext): Future[BatchUpdateId]

  def updateTracking(updateId: BatchUpdateId,
                     status: Status,
                     result: Option[Result],
                     problems: Option[Seq[Problem]])(implicit ec: ExecutionContext): Future[Int]
}

class EventTrackingRepositoryImpl(dbProvider: DatabaseProvider) extends EventTrackingRepository with LazyLogging {

  private val db = dbProvider.database
  private val dm = dbProvider.dataModel

  import dm.driver.api._

  override def createTracking(salesChannelId: SalesChannelId,
                              groupId: Option[RequestGroupId])(implicit ec: ExecutionContext): Future[BatchUpdateId] = {
    val newId = randomUUID
    db.run {
      dm.eventTrackings +=
        dm.EventTracking(
          id = newId,
          salesChannelId = salesChannelId.value,
          groupId = groupId.map(_.value),
          status = Pending.name,
          createdAt = now)
    }.map(_ => newId.id[UpdateInfos])
      .logOnFailure(ex => s"Failed to create Event Tracking entry: $ex")
  }

  override def updateTracking(updateId: BatchUpdateId,
                              status: Status,
                              result: Option[Result],
                              problems: Option[Seq[Problem]])(implicit ec: ExecutionContext): Future[Int] = {

    db.run {
      dm.eventTrackings.filter(_.id === updateId.value)
        .map(row => (row.status, row.result, row.problems, row.updatedAt))
        .update((status.name, result.map(_.name), problems.map(Json.toJson(_)), Option(now)))
    }.logOnFailure(ex => s"Failed to update Event Tracking entry: $ex")
  }

  private def now = new java.sql.Timestamp(System.currentTimeMillis)

}