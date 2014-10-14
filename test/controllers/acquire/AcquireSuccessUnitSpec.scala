package controllers.acquire

import controllers.acquire.Common.PrototypeHtml
import controllers.AcquireSuccess
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import helpers.common.CookieHelper.{fetchCookiesFromHeaders, verifyCookieHasBeenDiscarded}
import models.{BusinessKeeperDetailsFormModel, PrivateKeeperDetailsFormModel, NewKeeperDetailsViewModel, VehicleLookupFormModel, CompleteAndConfirmFormModel}
import org.mockito.Mockito.when
import pages.acquire.{VehicleLookupPage, BeforeYouStartPage}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import play.api.test.{FakeRequest, WithApplication}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.TraderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import utils.helpers.Config
import webserviceclients.fakes.FakeVehicleLookupWebService.RegistrationNumberValid
import pages.acquire.PrivateKeeperDetailsPage.{FirstNameValid, ModelValid}
import webserviceclients.fakes.FakeVehicleLookupWebService.VehicleMakeValid
import pages.acquire.SetupTradeDetailsPage.TraderBusinessNameValid
import CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey

class AcquireSuccessUnitSpec extends UnitSpec {

  "present" should {
    "display the page with new keeper cached" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false)

      val acquireSuccessPrototypeNotVisible = new AcquireSuccess()
      val result = acquireSuccessPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "redirect to before you start when no cookies are present" in new WithApplication {
      val request = FakeRequest()
      val result = acquireSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "redirect to before you start when no vehicle details cookie is present" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newPrivateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
      val result = acquireSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "redirect to before you start when no trader details cookie is present" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newPrivateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
      val result = acquireSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "redirect to before you start when no new keeper details cookie is present" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
      val result = acquireSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "redirect to before you start when no complete and confirm details cookie is present" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newPrivateKeeperDetailsModel())
      val result = acquireSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "present a full page with cached details when all cookies are present for new keeper success" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newPrivateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())

      val content = contentAsString(acquireSuccess.present(request))
      content should include(RegistrationNumberValid)
      content should include(VehicleMakeValid)
      content should include(ModelValid)
      content should include(FirstNameValid)
      content should include(YearDateOfSaleValid)
      content should include(MonthDateOfSaleValid)
      content should include(DayDateOfSaleValid)
    }
  }

  "buyAnother" should {
    "discard the vehicle, new keeper nd confirm cookies" in {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newPrivateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())

      val result = acquireSuccess.buyAnother(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)

        verifyCookieHasBeenDiscarded(VehicleLookupDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(VehicleLookupFormModelCacheKey, cookies)
        verifyCookieHasBeenDiscarded(NewKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(PrivateKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(BusinessKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(CompleteAndConfirmCacheKey, cookies)

        cookies.find(_.name == TraderDetailsCacheKey) should be(None)
      }
    }

    "redirect to the vehicle lookup page" in {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newPrivateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())

      val result = acquireSuccess.buyAnother(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  private val acquireSuccess = {
    injector.getInstance(classOf[AcquireSuccess])
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.newPrivateKeeperDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
    acquireSuccess.present(request)
  }
}