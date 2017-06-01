package services

import java.sql.Timestamp
import java.util.UUID

import api.common.Id._
import api.service.models.Info
import api.service.tags.ids._
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.JsObject
import services.slickbacked.{InfoRepository, InfoSlick}

import scala.concurrent.{ExecutionContext, Future}

trait InfoService {
  def list(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Seq[Info]]

  def insert(salesChannelId: SalesChannelId, id: UUID, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]]

  def update(salesChannelId: SalesChannelId, infoId: InfoId, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]]

  def getLastModifiedDate(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Option[DateTime]]
}

class InfoServiceImpl(infoRepository: InfoRepository) extends InfoService with LazyLogging {

  private def getCurrentTimeStamp: Timestamp = new Timestamp(new DateTime(DateTimeZone.UTC).getMillis)

  override def list(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Seq[Info]] =
    infoRepository.list(salesChannelId.value)
      .map(_.map(i => Info(id = Some(i.id.id[Info]), name = i.name, data = i.data.as[JsObject], meta = i.meta)))

  override def update(salesChannelId: SalesChannelId, infoId: InfoId, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]] =
    infoRepository.update(InfoSlick(
      id = infoId.value,
      lastModified = getCurrentTimeStamp,
      salesChannelId = salesChannelId.value,
      meta = info.meta.toList,
      name = info.name,
      data = info.data))

  override def insert(salesChannelId: SalesChannelId, id: UUID, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]] = {
    infoRepository.insert(InfoSlick(
      id = id,
      lastModified = getCurrentTimeStamp,
      salesChannelId = salesChannelId.value,
      meta = info.meta.toList,
      name = info.name,
      data = info.data)).map {
        case Left(s) => None
        case Right(i) => Some(i)
      }
  }

  override def getLastModifiedDate(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Option[DateTime]] =
    infoRepository.getLastModifiedDate(salesChannelId.value)
}
