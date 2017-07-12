package services

import java.sql.Timestamp
import java.util.UUID

import com.markland.service.Id._
import com.markland.service.models.{Info, InfoStatusEnum}
import com.markland.service.tags.ids
import com.markland.service.tags.ids._
import cache.LocalCache
import com.typesafe.scalalogging.LazyLogging
import models.QueryParams
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.JsObject
import services.slickbacked.{InfoRepository, InfoSlick}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scalaz.syntax.std.either._
import services.DateTimeHelper._

trait InfoService {

  def query(queryParams: QueryParams, limit: Int, startAfter: Option[InfoId] = None)(implicit ec: ExecutionContext): Future[Seq[Info]]

  def list(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Seq[Info]]

  def retrieve(salesChannelId: SalesChannelId, infoId: ids.InfoId)(implicit ec: ExecutionContext): Future[Option[Info]]

  def insert(salesChannelId: SalesChannelId, id: UUID, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]]

  def update(salesChannelId: SalesChannelId, infoId: InfoId, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]]

  def getLastModifiedDate(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Option[DateTime]]
}

class InfoServiceImpl(infoRepository: InfoRepository) extends InfoService with LazyLogging {

  private def getCurrentTimeStamp: Timestamp = new Timestamp(new DateTime(DateTimeZone.UTC).getMillis)

  private val cache = LocalCache[Option[Info]](1.hour, 10000)

  override def list(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Seq[Info]] =
    infoRepository.list(salesChannelId.value)
      .map(_.map(i => Info(
        id = Some(i.id.id[Info]),
        name = i.name,
        data = i.data.as[JsObject],
        meta = i.meta,
        status = InfoStatusEnum(i.status).disjunction.valueOr(e => sys.error(e.message)))))

  override def update(salesChannelId: SalesChannelId, infoId: InfoId, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]] =
    infoRepository.update(InfoSlick(
      id = infoId.value,
      lastModified = getCurrentTimeStamp,
      salesChannelId = salesChannelId.value,
      meta = info.meta.toList,
      name = info.name,
      data = info.data,
      status = info.status.name))

  override def insert(salesChannelId: SalesChannelId, id: UUID, info: Info)(implicit ec: ExecutionContext): Future[Option[UUID]] = {
    infoRepository.insert(InfoSlick(
      id = id,
      lastModified = getCurrentTimeStamp,
      salesChannelId = salesChannelId.value,
      meta = info.meta.toList,
      name = info.name,
      status = info.status.name,
      data = info.data)).map {
        case Left(s) => None
        case Right(i) => Some(i)
    }
  }

  override def getLastModifiedDate(salesChannelId: SalesChannelId)(implicit ec: ExecutionContext): Future[Option[DateTime]] =
    infoRepository.getLastModifiedDate(salesChannelId.value)

  override def retrieve(salesChannelId: SalesChannelId, infoId: ids.InfoId)(implicit ec: ExecutionContext): Future[Option[Info]] =
    cache.memo(salesChannelId.value.toString + infoId.value.toString) { _ =>
      infoRepository.retrieve(salesChannelId.value, infoId.value)
        .map(_.map(i => Info(
          id = Some(i.id.id[Info]),
          name = i.name,
          data = i.data.as[JsObject],
          meta = i.meta,
          status = InfoStatusEnum(i.status).disjunction.valueOr(e => sys.error(e.message)),
          lastModified = Some(i.lastModified.toDateTime))))
    }

  override def query(queryParams: QueryParams, limit: Int, startAfter: Option[InfoId])
                    (implicit ec: ExecutionContext): Future[Seq[Info]] = {
    infoRepository.query(queryParams, limit, startAfter).map(
      _.map(i => Info(
        id = Some(i.id.id[Info]),
        name = i.name,
        data = i.data.as[JsObject],
        meta = i.meta,
        status = InfoStatusEnum(i.status).disjunction.valueOr(e => sys.error(e.message))))
    )
  }
}
