package services

import java.util.UUID

import services.slickbacked.{DBSpecIT, SalesChannelsSlick}

import scala.concurrent.Await

class SalesChannelRepositorySpec extends DBSpecIT {

  import dataModel.driver.api._

  "SalesChannelRepository" should "return None if a sales channel record does not exist" in {

    val sc = salesChannelRepository.exists(UUID.fromString("F23E89E7-4162-432D-A7B0-F00181CF76FE"))
    sc.futureValue should === (None)
  }

  it should "return sales channel id if its record exists" in {
    val salesChannel: UUID = UUID.fromString("75506ce9-ece6-4835-bbb1-83613c326be7")
    val insert = dataModel.salesChannels += SalesChannelsSlick(salesChannel, "test")
    Await.ready(db.run(insert), waitDuration)

    val sc = salesChannelRepository.exists(salesChannel)
    sc.futureValue should === (Some(salesChannel))
  }

}
