package controllers

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import play.api.mvc.Result
import play.api.test.Helpers._
import setup.BaseSetup
import scala.concurrent.Future

class BaseControllerSpec extends FlatSpec
  with BaseSetup
  with Matchers
  with MockitoSugar
  with ScalaFutures {

  def validateResponses(expectedResponse: Future[Result], response: Future[Result]) = {
    val expectedResponseBody = contentAsString(expectedResponse)
    val responseBody = contentAsString(response)
    val expectedResponseHeaders = headers(expectedResponse)
    val responseHeaders = headers(response)

    expectedResponse.futureValue.header.status should === (response.futureValue.header.status)
    expectedResponseBody should === (responseBody)
    expectedResponseHeaders should === (responseHeaders)
  }
}
