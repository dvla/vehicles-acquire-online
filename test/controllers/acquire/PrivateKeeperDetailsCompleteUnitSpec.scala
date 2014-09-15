package controllers.acquire

import controllers.PrivateKeeperDetailsComplete
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import org.joda.time.LocalDate
import play.api.test.Helpers.{OK, contentAsString, defaultAwaitTimeout}
import play.api.test.{FakeRequest, WithApplication}


class PrivateKeeperDetailsCompleteUnitSpec extends UnitSpec {
  private val privateKeeperDetailsComplete = {
    injector.getInstance(classOf[PrivateKeeperDetailsComplete])
  }

  "Private keeper detail complete controller" should {
    "present an empty form" in new WithApplication {
      val request = FakeRequest()
      whenReady(privateKeeperDetailsComplete.present(request)) { r =>
        r.header.status should equal(OK)
      }
    }

    "present an a full form" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsCompleteModel(Some(new LocalDate(1234, 12, 24))))
      val html = contentAsString(privateKeeperDetailsComplete.present(request))
      html should include("24")
      html should include("12")
      html should include("1234")
    }
  }
}
