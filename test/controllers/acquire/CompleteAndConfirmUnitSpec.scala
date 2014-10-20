package controllers.acquire

import controllers.acquire.Common.PrototypeHtml
import controllers.{PrivateKeeperDetails, CompleteAndConfirm}
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import org.joda.time.Instant
import pages.acquire.BusinessKeeperDetailsPage.{BusinessNameValid, FleetNumberValid, EmailValid}
import pages.acquire.PrivateKeeperDetailsPage.{FirstNameValid, LastNameValid}
import models.CompleteAndConfirmFormModel.Form.{MileageId, DateOfSaleId, ConsentId}
import org.mockito.Mockito.when
import org.mockito.Matchers.any
import pages.acquire.AcquireSuccessPage
import pages.acquire.CompleteAndConfirmPage.{MileageValid, ConsentTrue}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import pages.acquire.VehicleLookupPage
import play.api.test.Helpers.{LOCATION, BAD_REQUEST, OK, contentAsString, defaultAwaitTimeout}
import play.api.test.{FakeRequest, WithApplication}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{DayId, MonthId, YearId}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import scala.concurrent.Future
import utils.helpers.Config
import webserviceclients.acquire.AcquireRequestDto
import webserviceclients.acquire.AcquireResponseDto
import webserviceclients.acquire.AcquireConfig
import webserviceclients.acquire.AcquireServiceImpl
import webserviceclients.acquire.AcquireService
import webserviceclients.acquire.AcquireWebService
import webserviceclients.fakes.FakeResponse
import webserviceclients.fakes.FakeAcquireWebServiceImpl.{acquireResponseSuccess, acquireResponseApplicationBeingProcessed}

class CompleteAndConfirmUnitSpec extends UnitSpec {

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

      val privateKeeperDetailsPrototypeNotVisible = new PrivateKeeperDetails()
      val result = privateKeeperDetailsPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "present a full form when new keeper, vehicle details and vehicle sorn cookies are present for new keeper" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
      val content = contentAsString(completeAndConfirm.present(request))
      content should include( s"""value="$MileageValid"""")
      content should include( """value="true"""") // Checkbox value
      content should include( s"""value="$YearDateOfSaleValid"""")
    }

    "display empty fields when new keeper complete cookie does not exist" in new WithApplication {
      val request = FakeRequest()
      val result = completeAndConfirm.present(request)
      val content = contentAsString(result)
      content should not include MileageValid
    }

    "redirect to vehicle lookup when no new keeper details cookie is present" in new WithApplication {
      val request = FakeRequest()
      val result = completeAndConfirm.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "play back business keeper details as expected" in new WithApplication() {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
        businessName = Some(BusinessNameValid),
        fleetNumber = Some(FleetNumberValid),
        email = Some(EmailValid),
        isBusinessKeeper = true
      )).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
      val content = contentAsString(completeAndConfirm.present(request))
      content should include("<dt>Fleet number</dt>")
      content should include(s"$BusinessNameValid")
      content should include(s"$FleetNumberValid")
      content should include(s"$EmailValid")
    }

    "play back private keeper details as expected" in new WithApplication() {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
        firstName = Some(FirstNameValid),
        lastName = Some(LastNameValid),
        email = Some(EmailValid),
        isBusinessKeeper = false
      )).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
      val content = contentAsString(completeAndConfirm.present(request))
      content should include(s"$FirstNameValid")
      content should include(s"$LastNameValid")
      content should include(s"$EmailValid")
    }
  }

  "submit" should {
    "replace numeric mileage error message for with standard error message" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(mileage = "$$").
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val result = completeAndConfirm.submit(request)
      val replacementMileageErrorMessage = "You must enter a valid mileage between 0 and 999999"
      replacementMileageErrorMessage.r.findAllIn(contentAsString(result)).length should equal(2)
    }

    "redirect to next page when mandatory fields are complete for new keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val acquireSuccess = acquireController(acquireWebService =
        acquireWebService(acquireServiceResponse = Some(acquireResponseApplicationBeingProcessed)))

      val result = acquireSuccess.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(AcquireSuccessPage.address))
      }
    }

    "redirect to next page when all fields are complete for new keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel()).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val acquireSuccess = acquireController(acquireWebService =
        acquireWebService(acquireServiceResponse = Some(acquireResponseApplicationBeingProcessed)))

      val result = acquireSuccess.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(AcquireSuccessPage.address))
      }
    }

    "return a bad request if consent is not ticked" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(consent = "").
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val result = completeAndConfirm.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }
  }

  private def acquireServiceMock(): AcquireService = {
    val acquireServiceMock = mock[AcquireService]
    when(acquireServiceMock.invoke(any[AcquireRequestDto], any[String])).
      thenReturn(Future.successful((0, None)))

    acquireServiceMock
  }

  private def acquireWebService(acquireServiceStatus: Int = OK,
                                acquireServiceResponse: Option[AcquireResponseDto] = Some(acquireResponseSuccess)): AcquireWebService = {
    val acquireWebService = mock[AcquireWebService]
    when(acquireWebService.callAcquireService(any[AcquireRequestDto], any[String])).
      thenReturn(Future.successful {
      val fakeJson = acquireServiceResponse map (Json.toJson(_))
      new FakeResponse(status = acquireServiceStatus, fakeJson = fakeJson) // Any call to a webservice will always return this successful response.
    })

    acquireWebService
  }

  private def dateServiceStub(dayToday: Int,
                              monthToday: Int,
                              yearToday: Int) = {
    val dayMonthYearStub = new DayMonthYear(day = dayToday,
      month = monthToday,
      year = yearToday)
    val dateService = mock[DateService]
    when(dateService.today).thenReturn(dayMonthYearStub)
    dateService
  }

  private val dateValid: String = DayMonthYear(
    1,
    1,
    2014
  ).toDateTime.get.toString

  private val emptySpace = " "

  private def dateServiceStubbed(day: Int = 1,
                                 month: Int = 1,
                                 year: Int = 2014) = {
    val dateService = mock[DateService]
    when(dateService.today).thenReturn(new DayMonthYear(day = day,
      month = month,
      year = year))

    val instant = new DayMonthYear(day = day,
      month = month,
      year = year).toDateTime.get.getMillis

    when(dateService.now).thenReturn(new Instant(instant))
    dateService
  }

  private val config: Config = {
    val config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(true)
    when(config.acquire).thenReturn(new AcquireConfig)
    config
  }

  private def acquireController(acquireWebService: AcquireWebService): CompleteAndConfirm = {
    val acquireService = new AcquireServiceImpl(config.acquire, acquireWebService)
    acquireController(acquireWebService, acquireService)
  }

  private def acquireController(acquireWebService: AcquireWebService, acquireService: AcquireService)
                               (implicit config: Config = config, dateService: DateService = dateServiceStubbed()):
  CompleteAndConfirm = {
    implicit val clientSideSessionFacAB12AWRtory = injector.getInstance(classOf[ClientSideSessionFactory])

    new CompleteAndConfirm(acquireService)
  }

  private def buildCorrectlyPopulatedRequest(mileage: String = MileageValid,
                                             dayDateOfSale: String = DayDateOfSaleValid,
                                             monthDateOfSale: String = MonthDateOfSaleValid,
                                             yearDateOfSale: String = YearDateOfSaleValid,
                                             consent: String = ConsentTrue) = {
    FakeRequest().withFormUrlEncodedBody(
      MileageId -> mileage,
      s"$DateOfSaleId.$DayId" -> dayDateOfSale,
      s"$DateOfSaleId.$MonthId" -> monthDateOfSale,
      s"$DateOfSaleId.$YearId" -> yearDateOfSale,
      ConsentId -> consent
    )
  }

  private def completeAndConfirm = {
    val config = new Config
    injector.getInstance(classOf[CompleteAndConfirm])
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
    completeAndConfirm.present(request)
  }
}