package services.slickbacked

import java.util.UUID.randomUUID

import com.markland.service.Id._
import com.markland.service.models.EnrichmentUpdateStatusStatusEnum.Pending
import com.markland.service.models.UpdateInfos
import com.markland.service.tags.ids.{BatchUpdateId, RequestGroupId, SalesChannelId}
import com.typesafe.scalalogging.LazyLogging
import services.slickbacked.FutureUtils._

import scala.concurrent.{ExecutionContext, Future}

trait EventTrackingRepository {
  def createTracking(salesChannelId: SalesChannelId,
                     groupId: Option[RequestGroupId])(implicit ec: ExecutionContext): Future[BatchUpdateId]
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

  private def now = new java.sql.Timestamp(System.currentTimeMillis)

}