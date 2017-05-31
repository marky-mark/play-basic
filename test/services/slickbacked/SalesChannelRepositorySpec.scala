package services.slickbacked

import java.util.UUID

import scala.concurrent.Await

class SalesChannelRepositorySpec extends DBSpec {

  import dataModel.driver.api._

  "SalesChannelRepository" should "return None if a sales channel record does not exist" in {

    val sc = salesChannelRepository.exists(baseSalesChannelId)
    sc.futureValue should === (None)
  }

  it should "return sales channel id if its record exists" in {
    val salesChannel: UUID = UUID.fromString("75506ce9-ece6-4835-bbb1-83613c326be7")
    val insert = dataModel.salesChannels += SalesChannels(salesChannel, "test")
    Await.ready(db.run(insert), waitDuration)

    val sc = salesChannelRepository.exists(salesChannel)
    sc.futureValue should === (Some(salesChannel))
  }

}