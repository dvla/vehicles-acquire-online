package controllers.acquire

import controllers.KeeperStillOnRecord
import helpers.CookieHelper.{fetchCookiesFromHeaders, verifyCookieHasBeenDiscarded}
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import pages.acquire.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage}
import play.api.test.Helpers.{LOCATION, OK, SEE_OTHER}
import play.api.test.{FakeRequest, WithApplication}
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.VehicleAndKeeperLookupDetailsCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.SetupTradeDetailsFormModel.SetupTradeDetailsCacheKey
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey

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
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupResponseCode())
      val result = keeperStillOnRecord.buyAnotherVehicle(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.size should equal(3)
        verifyCookieHasBeenDiscarded(VehicleLookupFormModelCacheKey, cookies)
        verifyCookieHasBeenDiscarded(VehicleAndKeeperLookupDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(VehicleLookupResponseCodeCacheKey, cookies)

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
      val result = keeperStillOnRecord.finish(request)

      whenReady(result) { r =>
        r.header.status should equal(SEE_OTHER)
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))

        val cookies = fetchCookiesFromHeaders(r)
        val cookieNames = List(
          SetupTradeDetailsCacheKey,
          BusinessChooseYourAddressCacheKey,
          VehicleLookupFormModelCacheKey,
          VehicleAndKeeperLookupDetailsCacheKey
        )
        cookieNames.foreach(verifyCookieHasBeenDiscarded(_, cookies))
      }
    }
  }

  private lazy val keeperStillOnRecord = {
    injector.getInstance(classOf[KeeperStillOnRecord])
  }

  private lazy val present = {
    val request = FakeRequest()
      .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    keeperStillOnRecord.present(request)
  }

  private lazy val presentWithNoCookies = {
    val request = FakeRequest()
    keeperStillOnRecord.present(request)
  }
}