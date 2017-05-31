package services.slickbacked

import java.sql.Timestamp
import java.util.UUID

import play.api.libs.json.JsValue

case class SalesChannels(salesChannelId: UUID, name: String)

case class Info(id: UUID,
                name: String,
                data: JsValue,
                meta: List[String],
                lastModified: Timestamp,
                salesChannelId: UUID)

class DataModel(val driver: ExtendedPostgresDriver) {

  import driver.api._

  case class InfoTable(tag: Tag) extends Table[Info](tag, "info") {
    def id = column[UUID]("id")
    def name = column[String]("name")
    def data = column[JsValue]("data")
    def meta = column[List[String]]("meta")
    def lastModified = column[Timestamp]("last_modified")
    def salesChannelId = column[UUID]("sales_channel_id")

    def pk = primaryKey("info_pkey", id)
    def fk = foreignKey("info_fkey", salesChannelId, salesChannels)(_.id, onDelete = ForeignKeyAction.Cascade)
    def * = (id, name, data, meta, lastModified, salesChannelId) <> (Info.tupled, Info.unapply _)
  }

  case class SalesChannelsTable (tag: Tag) extends Table[SalesChannels](tag, "sales_channels") {
    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[String]("name")
    def * = (id, name) <> (SalesChannels.tupled, SalesChannels.unapply)
  }

  lazy val info = TableQuery[InfoTable]
  lazy val salesChannels = TableQuery[SalesChannelsTable]

}
