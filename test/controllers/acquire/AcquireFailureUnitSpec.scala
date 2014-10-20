package controllers.acquire

import controllers.AcquireFailure
import controllers.acquire.Common.PrototypeHtml
import helpers.common.CookieHelper.{verifyCookieHasBeenDiscarded, fetchCookiesFromHeaders}
import helpers.{UnitSpec, WithApplication}
import helpers.acquire.CookieFactoryForUnitSpecs
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import models.NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import org.joda.time.format.DateTimeFormat
import pages.acquire.PrivateKeeperDetailsPage.{EmailValid, LastNameValid, FirstNameValid}
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.TraderDetailsCacheKey
import org.mockito.Mockito.when
import pages.acquire.{BeforeYouStartPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import utils.helpers.Config
import webserviceclients.fakes.FakeVehicleLookupWebService.{TransactionIdValid, TransactionTimestampValid}

final class AcquireFailureUnitSpec extends UnitSpec {
  "present" should {
    "display the page" in new WithApplication {
//      whenReady(present) { r =>
//        r.header.status should equal(OK)
//      }
    }

    "not display progress bar" in new WithApplication {
      contentAsString(present) should not include "Step "
    }

    "display prototype message when config set to true" in new WithApplication {
//      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
//      val request = FakeRequest()
//      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
//      implicit val config: Config = mock[Config]
//      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
//      val AcquireFailurePrototypeNotVisible = new AcquireFailure()
//
//      val result = AcquireFailurePrototypeNotVisible.present(request)
//      contentAsString(result) should not include PrototypeHtml
    }

    "present a full page when all cookies are present for failure" in new WithApplication {
      val fmt = DateTimeFormat.forPattern("dd/MM/yyyy")

      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
        firstName = Some(FirstNameValid),
        lastName = Some(LastNameValid),
        email = Some(EmailValid)
      ))

      val content = contentAsString(acquireFailure.present(request))
      content should include(fmt.print(TransactionTimestampValid))
      content should include(TransactionIdValid)
    }
  }

  "buyAnother" should {
    "discard the vehicle, new keeper and confirm cookies" in {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val result = acquireFailure.buyAnother(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)

        verifyCookieHasBeenDiscarded(VehicleLookupDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(VehicleLookupFormModelCacheKey, cookies)
        verifyCookieHasBeenDiscarded(NewKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(PrivateKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(BusinessKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(CompleteAndConfirmCacheKey, cookies)
        verifyCookieHasBeenDiscarded(AcquireCompletionResponseCacheKey, cookies)

        cookies.find(_.name == TraderDetailsCacheKey) should be(None)
      }
    }

    "redirect to the vehicle lookup page" in {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val result = acquireFailure.buyAnother(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  "finish" should {
    "discard the vehicle, new keeper and confirm cookies" in {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

      val result = acquireFailure.finish(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)

        verifyCookieHasBeenDiscarded(VehicleLookupDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(VehicleLookupFormModelCacheKey, cookies)
        verifyCookieHasBeenDiscarded(NewKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(PrivateKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(BusinessKeeperDetailsCacheKey, cookies)
        verifyCookieHasBeenDiscarded(CompleteAndConfirmCacheKey, cookies)
        verifyCookieHasBeenDiscarded(AcquireCompletionResponseCacheKey, cookies)

        cookies.find(_.name == TraderDetailsCacheKey) should be(None)
      }
    }

    "redirect to the before you start page" in {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())

      val result = acquireFailure.finish(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private val acquireFailure = {
    injector.getInstance(classOf[AcquireFailure])
  }


  private lazy val present = {
    val AcquireFailure = injector.getInstance(classOf[AcquireFailure])
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())
    AcquireFailure.present(request)
  }
}