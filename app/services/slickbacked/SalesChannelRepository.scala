package services.slickbacked

import java.util.UUID

import cache.{Caching, CachingContext}

import scala.concurrent.{ExecutionContext, Future}

trait SalesChannelRepository {
  def exists(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Option[UUID]]
}

class SalesChannelRepositoryImpl(dbProvider: DatabaseProvider, val cachingContext: CachingContext) extends SalesChannelRepository
  with Caching[Option[UUID]] {

  private val db = dbProvider.database
  private val dm = dbProvider.dataModel

  import dm.driver.api._

  override val cachingPrefix = "SalesChannel"
  override val metricsPrefix = "SalesChannel"

  override def exists(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Option[UUID]] = {
    caching("info") {
      val salesChannelExistQuery = dm.salesChannels.filter(_.id === salesChannelId).exists.result.flatMap { exists =>
        if (!exists) DBIO.successful(None)
        else DBIO.successful(Some(salesChannelId))
      }
      db.run(salesChannelExistQuery)
    }
  }

}
