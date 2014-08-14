package controllers.acquire

import helpers.UnitSpec
import controllers.SetUpTradeDetails
import play.api.test.{WithApplication, FakeRequest}
import play.api.test.Helpers._


class SetupTradeDetailsUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) {
        r =>
          r.header.status should equal(OK)
      }
    }
  }

  private val setUpTradeDetails = {
    injector.getInstance(classOf[SetUpTradeDetails])
  }

  private lazy val present = {
    val request = FakeRequest()
    setUpTradeDetails.present(request)
  }
}