package services.slickbacked

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

trait SalesChannelRepository {
  def exists(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Option[UUID]]
}

class SalesChannelRepositoryImpl(dbProvider: DatabaseProvider) extends SalesChannelRepository {

  private val db = dbProvider.database
  private val dm = dbProvider.dataModel

  import dm.driver.api._

  override def exists(salesChannelId: UUID)(implicit ec: ExecutionContext): Future[Option[UUID]] = {
    val salesChannelExistQuery = dm.salesChannels.filter(_.id === salesChannelId).exists.result.flatMap { exists =>
      if (!exists) DBIO.successful(None)
      else DBIO.successful(Some(salesChannelId))
    }
    db.run(salesChannelExistQuery)
  }

}
