package services

import java.sql.Timestamp
import java.util.UUID

import api.common.Id._
import api.service.models.Info
import api.service.tags.ids._
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.JsObject
import services.slickbacked.{InfoRepository, InfoSlick}

import scala.concurrent.{ExecutionContext, Future}

trait InfoService {
  def list(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Seq[Info]]

  def insert(salesChannelId: SalesChannelId, info: Info)(implicit ec: ExecutionContext): Future[Either[String,UUID]] //just doing an Either for fun

  def update(salesChannelId: SalesChannelId, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]]

  def getLastModifiedDate(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Option[DateTime]]
}

class InfoServiceImpl(infoRepository: InfoRepository) extends InfoService {

  private def getCurrentTimeStamp: Timestamp = new Timestamp(new DateTime(DateTimeZone.UTC).getMillis)

  override def list(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Seq[Info]] =
    infoRepository.list(salesChannelId.value)
      .map(_.map(i => Info(id = Some(i.id.id[Info]), name = i.name, data = i.data.as[JsObject], meta = i.meta)))

  override def update(salesChannelId: SalesChannelId, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]] =
    infoRepository.update(InfoSlick(
      id = info.id.get.value,
      lastModified = getCurrentTimeStamp,
      salesChannelId = salesChannelId.value,
      meta = info.meta.toList,
      name = info.name,
      data = info.data))

  override def insert(salesChannelId: SalesChannelId, info: Info)(implicit ec: ExecutionContext): Future[Either[String, UUID]] =
    infoRepository.insert(InfoSlick(
      id = info.id.get.value,
      lastModified = getCurrentTimeStamp,
      salesChannelId = salesChannelId.value,
      meta = info.meta.toList,
      name = info.name,
      data = info.data))

  override def getLastModifiedDate(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Option[DateTime]] =
    infoRepository.getLastModifiedDate(salesChannelId.value)
}