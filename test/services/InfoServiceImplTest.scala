package services

import java.util.UUID

import com.markland.service.Id._
import com.markland.service.models.{Info, InfoStatusEnum}
import org.joda.time.format.DateTimeFormat
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, Json}
import services.slickbacked.{InfoRepository, InfoSlick}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class InfoServiceImplTest extends FlatSpec
  with Matchers
  with MockitoSugar
  with ScalaFutures  {

  def dataJson = Json.parse(
    s"""
       |{
       |   "foo": "bar"
       |
       |}
    """.stripMargin
  )

  it should "Translate the object" in {

    val mockRepo = mock[InfoRepository]

    val service = new InfoServiceImpl(mockRepo)

    val marketplaceToBeStored = Info(Some(UUID.randomUUID().id), "foo", dataJson.as[JsObject], List("foo", "bar"),
      InfoStatusEnum.Inactive,
      Some(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime("04/02/2011 20:27:05")))

    when(mockRepo.insert(any(classOf[InfoSlick]))(any(classOf[ExecutionContext]))).thenReturn(Future.successful {
      Right(marketplaceToBeStored.id.get.value)
    })

    service.insert(UUID.randomUUID().id,UUID.randomUUID() ,marketplaceToBeStored)

    val captor: ArgumentCaptor[InfoSlick] = ArgumentCaptor.forClass(classOf[InfoSlick])

    verify(mockRepo).insert(captor.capture())(any(classOf[ExecutionContext]))

    val receivedMarketplace = captor.getValue

    receivedMarketplace.status should ===("inactive")
    receivedMarketplace.id should not be marketplaceToBeStored.id
    receivedMarketplace.lastModified should not be marketplaceToBeStored.lastModified
    receivedMarketplace.name should ===(marketplaceToBeStored.name)

  }

}
