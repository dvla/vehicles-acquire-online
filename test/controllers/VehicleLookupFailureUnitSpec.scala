package controllers

import Common.PrototypeHtml
import helpers.TestWithApplication
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import org.mockito.Mockito.when
import pages.acquire.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

class VehicleLookupFailureUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to setup trader details on if traderDetailsModel is not in cache" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.bruteForcePreventionViewModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      val result = vehicleLookupFailure.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to setup trader details on if bruteForcePreventionViewModel is not in cache" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      val result = vehicleLookupFailure.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to setup trader details on if VehicleLookupFormModelCache is not in cache" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.bruteForcePreventionViewModel())
      val result = vehicleLookupFailure.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new TestWithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      val vehicleLookupFailurePrototypeNotVisible = new VehicleLookupFailure()

      val result = vehicleLookupFailurePrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to vehicle lookup on submit" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      val result = vehicleLookupFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to setup trader details on submit when cache is empty" in new TestWithApplication {
      val request = FakeRequest()
      val result = vehicleLookupFailure.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private lazy val vehicleLookupFailure = {
    injector.getInstance(classOf[VehicleLookupFailure])
  }

  private lazy val present = {
    val request = FakeRequest()
      .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      .withCookies(CookieFactoryForUnitSpecs.bruteForcePreventionViewModel())
      .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      .withCookies(CookieFactoryForUnitSpecs.vehicleLookupResponse())
    vehicleLookupFailure.present(request)
  }
}
