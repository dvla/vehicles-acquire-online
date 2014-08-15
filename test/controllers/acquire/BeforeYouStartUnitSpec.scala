package controllers.acquire

import helpers.UnitSpec
import play.api.test.{WithApplication, FakeRequest}
import play.api.test.Helpers._
import controllers.BeforeYouStart
import controllers.acquire.Common._

class BeforeYouStartUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      val result = beforeYouStart.present(FakeRequest())
      status(result) should equal(OK)
    }

    "display prototype message when config set to true" in new WithApplication {
      val result = beforeYouStart.present(FakeRequest())
      contentAsString(result) should include(PrototypeHtml)
    }

  }



  private val beforeYouStart = injector.getInstance(classOf[BeforeYouStart])
}
