package controllers

import com.tzavellas.sse.guice.ScalaModule
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
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito.when
import pages.acquire.BusinessKeeperDetailsPage.{BusinessNameValid, FleetNumberValid}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import pages.acquire.PrivateKeeperDetailsPage.{EmailValid, FirstNameValid, LastNameValid}
import pages.acquire.{BeforeYouStartPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import webserviceclients.fakes.FakeDateServiceImpl
import scala.concurrent.duration.DurationInt
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.BusinessKeeperDetailsFormModel.businessKeeperDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.NewKeeperDetailsViewModel.newKeeperDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.PrivateKeeperDetailsFormModel.privateKeeperDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.traderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.services.{DateService, DateServiceImpl}
import utils.helpers.Config
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.RegistrationNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.TransactionIdValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.TransactionTimestampValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.VehicleMakeValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.VehicleModelValid

class AcquireSuccessUnitSpec extends UnitSpec {

  implicit val dateService = new DateServiceImpl
  val testDuration = 7.days.toMillis

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
      implicit val surveyUrl = new SurveyUrl()
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

      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
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

      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
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

    "offer the survey on first successful dispose" in new WithApplication {
      implicit val config = mockSurveyConfig()
      val acquireSuccess = acquireWithMockConfig(config)
      contentAsString(acquireSuccess.present(requestFullyPopulated)) should include(config.surveyUrl.get)
    }

    "not offer the survey for one just after the initial survey offering" in new WithApplication {
      implicit val config = mockSurveyConfig()
      val aMomentAgo = (Instant.now.getMillis - 100).toString

      val acquireSuccess = acquireWithMockConfig(config)
      contentAsString(acquireSuccess.present(
        requestFullyPopulated.withCookies(CookieFactoryForUnitSpecs.surveyUrl(aMomentAgo))
      )) should not include config.surveyUrl.get
    }

    "offer the survey one week after the first offering" in new WithApplication {
      implicit val config = mockSurveyConfig()
      val moreThen7daysAgo = (Instant.now.getMillis - config.surveyInterval - 1.minute.toMillis).toString

      val acquireSuccess = acquireWithMockConfig(config)
      contentAsString(acquireSuccess.present(
        requestFullyPopulated.withCookies(CookieFactoryForUnitSpecs.surveyUrl(moreThen7daysAgo))
      )) should include(config.surveyUrl.get)
    }

    "not offer the survey less than one week after the first offering" in new WithApplication {
      implicit val config = mockSurveyConfig()
      val lessThen7daysАgo = (Instant.now.getMillis - config.surveyInterval + 1.minute.toMillis).toString

      val acquireSuccess = acquireWithMockConfig(config)
      contentAsString(acquireSuccess.present(
        requestFullyPopulated.withCookies(CookieFactoryForUnitSpecs.surveyUrl(lessThen7daysАgo))
      )) should not include config.surveyUrl.get
    }

    "not offer the survey if the survey url is not set in the config" in new WithApplication {
      implicit val config: Config = mockSurveyConfig(None)
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val surveyUrl = new SurveyUrl()(clientSideSessionFactory, config, new FakeDateServiceImpl)
      implicit val dateService = injector.getInstance(classOf[DateService])

      val acquireSuccess = acquireWithMockConfig(config)
      val presentFake = acquireSuccess.present(requestFullyPopulated)
      contentAsString(presentFake) should not include "survey"
    }
  }

  "buyAnother" should {
    "discard the vehicle, new keeper and confirm cookies" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

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
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())

      val result = acquireSuccess.buyAnother(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  "finish" should {
    "discard the vehicle, new keeper and confirm cookies" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())

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
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())

      val result = acquireSuccess.finish(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BeforeYouStartPage.address))
      }
    }
  }

  private lazy val acquireSuccess = {
    injector.getInstance(classOf[AcquireSuccess])
  }

  // Must be lazy otherwise java.lang.RuntimeException: There is no started application is thrown
  private lazy val requestFullyPopulated = FakeRequest()
    .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
    .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
    .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
    .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
    .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmResponseModelModel())

  // Must be lazy otherwise java.lang.RuntimeException: There is no started application is thrown
  private lazy val present = {
    acquireSuccess.present(requestFullyPopulated)
  }

  def acquireWithMockConfig(config: Config): AcquireSuccess =
    testInjector(new ScalaModule() {
      override def configure(): Unit = bind[Config].toInstance(config)
    }).getInstance(classOf[AcquireSuccess])

  def mockSurveyConfig(url: Option[String] = Some("http://test/survery/url")): Config = {
    val config = mock[Config]
    val surveyUrl = url
    when(config.surveyUrl).thenReturn(surveyUrl)
    when(config.surveyInterval).thenReturn(testDuration)
    when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
    when(config.assetsUrl).thenReturn(None) // Stub this config value.
    config
  }
}
