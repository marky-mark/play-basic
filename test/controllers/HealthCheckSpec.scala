package controllers

import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._


class HealthCheckSpec extends BaseControllerSpec {

  val controller = new HealthCheck()
  val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "Health" should "return OK for health check" in {
    val response = controller.get().apply(getRequest)
    status(response) should === (Status.OK)
  }
}
