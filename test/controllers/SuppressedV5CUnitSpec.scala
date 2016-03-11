package controllers

import composition.WithApplication
import helpers.acquire.CookieFactoryForUnitSpecs
import helpers.UnitSpec
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import pages.acquire.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, SEE_OTHER}
import uk.gov.dvla.vehicles.presentation.common
import common.model.MicroserviceResponseModel.MsResponseCacheKey
import common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import common.testhelpers.CookieHelper.verifyCookieHasBeenDiscarded

class SuppressedV5CUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to setup trade details when no cookies are in the request" in new WithApplication {
      whenReady(presentWithNoCookies) { r =>
        r.header.status should equal(SEE_OTHER)
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }
  }

  "clicking buyAnotherVehicle button" should {
    "remove all vehicle related cookies and redirect to vehicle lookup page" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupResponse())
      val result = suppressedV5C.buyAnotherVehicle(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.size should equal(3)
        verifyCookieHasBeenDiscarded(VehicleLookupFormModelCacheKey, cookies)
        verifyCookieHasBeenDiscarded(vehicleAndKeeperLookupDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(MsResponseCacheKey, cookies)

        r.header.status should equal(SEE_OTHER)
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  "clicking finish button" should {
    "move to the before you start page and remove cookies" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = suppressedV5C.finish(request)

      whenReady(result) { r =>
        r.header.status should equal(SEE_OTHER)
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))

        val cookies = fetchCookiesFromHeaders(r)
        val cookieNames = List(
          BusinessChooseYourAddressCacheKey,
          VehicleLookupFormModelCacheKey,
          vehicleAndKeeperLookupDetailsCacheKey
        )
        cookieNames.foreach(verifyCookieHasBeenDiscarded(_, cookies))
      }
    }
  }

  private lazy val suppressedV5C = {
    injector.getInstance(classOf[SuppressedV5C])
  }

  private lazy val present = {
    val request = FakeRequest()
      .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    suppressedV5C.present(request)
  }

  private lazy val presentWithNoCookies = {
    val request = FakeRequest()
    suppressedV5C.present(request)
  }
}
