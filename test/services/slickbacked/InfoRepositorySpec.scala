package services.slickbacked

import java.sql.Timestamp
import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Await

class InfoRepositorySpec extends DBSpecIT {

  import dataModel.driver.api._

  def dataJson = Json.parse(
    s"""
       |{
       |   "foo": "bar"
       |
       |}
    """.stripMargin
  )

  it should "save an info record into the database" in {

    val expectedId = UUID.fromString("62729342-A89D-401A-8B42-32BD15E01220")

    val info = InfoSlick(id = expectedId, name = "foo", data = dataJson.as[JsObject], meta = List("foo", "bar"), lastModified = getCurrentTimeStamp, salesChannelId = baseSalesChannelId)

    val result = infoRepository.insert(info)
    result.futureValue.right.get should ===(expectedId)

    val infoRetrieved = infoRepository.retrieve(baseSalesChannelId, expectedId).futureValue

    infoRetrieved.get should ===(info)
  }

  it should "update info record in the database" in {

    val expectedId1 = UUID.fromString("62729342-A89D-401A-8B42-32BD15E01220")
    val info1 = InfoSlick(expectedId1, "foo", dataJson.as[JsObject], List("foo", "bar"), getCurrentTimeStamp, baseSalesChannelId)
    val insert1 = infoRepository.insert(info1)
    Await.ready(insert1, waitDuration)

    val expectedId2 = UUID.fromString("2A41F667-F9C9-4F79-A46F-A0758D1E0672")
    val info2 = InfoSlick(expectedId2, "bar", dataJson.as[JsObject], List("foo", "bar", "zoo"), getCurrentTimeStamp, baseSalesChannelId)
    val insert2 = infoRepository.insert(info2)
    Await.ready(insert2, waitDuration)

    val info1Updated = info1.copy(meta = List("replaced"), lastModified = getCurrentTimeStamp)
    val update = infoRepository.update(info1Updated)
    Await.ready(update, waitDuration)

    val infos = infoRepository.list(baseSalesChannelId).futureValue
    infos.size should ===(2)

    infos.filter(_.id == expectedId1).head should ===(info1Updated)
  }

  private def getCurrentTimeStamp: Timestamp = new Timestamp(new DateTime(DateTimeZone.UTC).getMillis)

  it should "return last modified date" in {

    val date1 = new DateTime(2017, 5, 1, 12, 20, DateTimeZone.UTC)
    val date2 = new DateTime(2016, 5, 1, 12, 20, DateTimeZone.UTC)
    val date3 = new DateTime(2015, 5, 1, 12, 20, DateTimeZone.UTC)

    val info1 = InfoSlick(UUID.randomUUID(), "foo", dataJson.as[JsObject], List("foo", "bar"), new Timestamp(date1.getMillis), baseSalesChannelId)
    val info2 = info1.copy(id = UUID.randomUUID, lastModified = new Timestamp(date2.getMillis))
    val info3 = info1.copy(id = UUID.randomUUID, lastModified = new Timestamp(date3.getMillis))

    val rules = Seq(info1, info2, info3)
    val insertResult = db.run(DBIO.sequence(rules.map(i => dataModel.info += i))).futureValue
    insertResult.sum should ===(3)

    val lastModifiedDate = infoRepository.getLastModifiedDate(baseSalesChannelId).futureValue
    lastModifiedDate should ===(Some(date1))

  }

}
