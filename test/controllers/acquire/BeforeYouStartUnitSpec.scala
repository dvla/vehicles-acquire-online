package controllers.acquire

import controllers.acquire.Common.PrototypeHtml
import controllers.BeforeYouStart
import helpers.UnitSpec
import org.mockito.Mockito.when
import pages.acquire.SetupTradeDetailsPage
import play.api.test.{FakeRequest, WithApplication}
import play.api.test.Helpers.{OK, LOCATION, contentAsString, defaultAwaitTimeout, status}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

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

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
      when(config.assetsUrl).thenReturn(None) // Stub this config value.
      val beforeYouStartPrototypeNotVisible = new BeforeYouStart()

      val result = beforeYouStartPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to next page after the button is clicked" in new WithApplication {
      val result = beforeYouStart.submit(FakeRequest())
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }
  }

  private lazy val beforeYouStart = injector.getInstance(classOf[BeforeYouStart])
}