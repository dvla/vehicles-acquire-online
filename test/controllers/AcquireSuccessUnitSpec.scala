package controllers

import composition.WithApplication
import Common.PrototypeHtml
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import helpers.common.CookieHelper.{fetchCookiesFromHeaders, verifyCookieHasBeenDiscarded}
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito.when
import pages.acquire.BusinessKeeperDetailsPage.{BusinessNameValid, FleetNumberValid}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import pages.acquire.PrivateKeeperDetailsPage.{EmailValid, FirstNameValid, LastNameValid}
import pages.acquire.{BeforeYouStartPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.BusinessKeeperDetailsFormModel.businessKeeperDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.NewKeeperDetailsViewModel.newKeeperDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.PrivateKeeperDetailsFormModel.privateKeeperDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.traderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import utils.helpers.Config
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.{RegistrationNumberValid, TransactionIdValid, TransactionTimestampValid, VehicleMakeValid, VehicleModelValid}

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

    "redirect to before you start when no completion cookie is present" in new WithApplication {
      val request = FakeRequest()
      val result = acquireSuccess.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }

    "present a full page with private keeper cached details when all cookies are present for new keeper success" in new WithApplication {
      val fmt = DateTimeFormat.forPattern("dd/MM/yyyy")

      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
          firstName = Some(FirstNameValid),
          lastName = Some(LastNameValid),
          email = Some(EmailValid)
        ))

      val content = contentAsString(acquireSuccess.present(request))
      content should include(RegistrationNumberValid)
      content should include(VehicleMakeValid)
      content should include(VehicleModelValid)
      content should include(FirstNameValid)
      content should include(LastNameValid)
      content should include(EmailValid)
      content should include(YearDateOfSaleValid)
      content should include(MonthDateOfSaleValid)
      content should include(DayDateOfSaleValid)
      content should include(fmt.print(TransactionTimestampValid))
      content should include(TransactionIdValid)
    }

    "present a full page with business keeper cached details when all cookies are present for new keeper success" in new WithApplication {
      val fmt = DateTimeFormat.forPattern("dd/MM/yyyy")

      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
          businessName = Some(BusinessNameValid),
          fleetNumber = Some(FleetNumberValid),
          email = Some(EmailValid)
        ))

      val content = contentAsString(acquireSuccess.present(request))
      content should include(RegistrationNumberValid)
      content should include(VehicleMakeValid)
      content should include(VehicleModelValid)
      content should include(BusinessNameValid)
      content should include(EmailValid)
      content should include(YearDateOfSaleValid)
      content should include(MonthDateOfSaleValid)
      content should include(DayDateOfSaleValid)
      content should include(fmt.print(TransactionTimestampValid))
      content should include(TransactionIdValid)
    }
}

  "buyAnother" should {
    "discard the vehicle, new keeper and confirm cookies" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val result = acquireSuccess.buyAnother(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)

        verifyCookieHasBeenDiscarded(vehicleAndKeeperLookupDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(VehicleLookupFormModelCacheKey, cookies)
        verifyCookieHasBeenDiscarded(newKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(privateKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(businessKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(CompleteAndConfirmCacheKey, cookies)
        verifyCookieHasBeenDiscarded(AcquireCompletionResponseCacheKey, cookies)

          cookies.find(_.name == traderDetailsCacheKey) should be(None)
      }
    }

    "redirect to the vehicle lookup page" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())

      val result = acquireSuccess.buyAnother(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  "finish" should {
    "discard the vehicle, new keeper and confirm cookies" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val result = acquireSuccess.finish(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)

        verifyCookieHasBeenDiscarded(vehicleAndKeeperLookupDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(VehicleLookupFormModelCacheKey, cookies)
        verifyCookieHasBeenDiscarded(newKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(privateKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(businessKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(CompleteAndConfirmCacheKey, cookies)
        verifyCookieHasBeenDiscarded(AcquireCompletionResponseCacheKey, cookies)
        verifyCookieHasBeenDiscarded(VehicleTaxOrSornCacheKey, cookies)
        verifyCookieHasBeenDiscarded(AcquireCompletionResponseCacheKey, cookies)
      }
    }

    "redirect to the before you start page" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())

      val result = acquireSuccess.finish(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private lazy val acquireSuccess = {
    injector.getInstance(classOf[AcquireSuccess])
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
      withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel()).
      withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())
    acquireSuccess.present(request)
  }
}
