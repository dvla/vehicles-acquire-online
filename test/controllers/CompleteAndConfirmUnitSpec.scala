package controllers

import composition.WithApplication
import Common.PrototypeHtml
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import java.util.TimeZone
import models.CompleteAndConfirmFormModel.Form.{ConsentId, DateOfSaleId, MileageId}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeZone, DateTime, Instant}
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import pages.acquire.BusinessKeeperDetailsPage.{BusinessNameValid, EmailValid, FleetNumberValid}
import pages.acquire.CompleteAndConfirmPage.ConsentTrue
import pages.acquire.CompleteAndConfirmPage.DayDateOfSaleValid
import pages.acquire.CompleteAndConfirmPage.MileageValid
import pages.acquire.CompleteAndConfirmPage.MonthDateOfSaleValid
import pages.acquire.CompleteAndConfirmPage.YearDateOfSaleValid
import pages.acquire.PrivateKeeperDetailsPage.{FirstNameValid, LastNameValid}
import pages.acquire.{AcquireSuccessPage, SetupTradeDetailsPage, VehicleLookupPage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, contentAsString, defaultAwaitTimeout, LOCATION, OK}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{TrackingId, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{DayId, MonthId, YearId}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireRequestDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireResponseDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireWebService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendRequest
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailServiceSendResponse
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.fakes.FakeAcquireWebServiceImpl.{acquireResponseApplicationBeingProcessed, acquireResponseSuccess}
import webserviceclients.fakes.FakeResponse

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
      implicit val dateService = injector.getInstance(classOf[DateService])
      when(config.isPrototypeBannerVisible).thenReturn(false)

      val privateKeeperDetailsPrototypeNotVisible = new PrivateKeeperDetails()
      val result = privateKeeperDetailsPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "present a full form when new keeper, vehicle details and vehicle sorn cookies are present for new keeper" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
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

    "play back new keeper details as expected" in new WithApplication() {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
        businessName = Some(BusinessNameValid),
        fleetNumber = Some(FleetNumberValid),
        email = Some(EmailValid),
        isBusinessKeeper = true
      )).withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
      val content = contentAsString(completeAndConfirm.present(request))
      content should include("<dt>Fleet number</dt>")
      content should include(s"$BusinessNameValid")
      content should include(s"$FleetNumberValid")
      content should include(s"$EmailValid")
    }

    "play back private keeper details as expected" in new WithApplication() {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
        firstName = Some(FirstNameValid),
        lastName = Some(LastNameValid),
        email = Some(EmailValid),
        isBusinessKeeper = false
      )).withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
      val content = contentAsString(completeAndConfirm.present(request))
      content should include(s"$FirstNameValid")
      content should include(s"$LastNameValid")
      content should include(s"$EmailValid")
    }
  }

  "submit" should {
    "redirect to SetUpTradeDetails when trader details cookie is missing" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val result = completeAndConfirm.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to VehicleLookup when new keeper details cookie data is missing" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val result = completeAndConfirm.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to VehicleLookup when vehicle lookup cookie data is missing" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val result = completeAndConfirm.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to VehicleLookup when vehicle and keeper details cookie data is missing" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val result = completeAndConfirm.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to VehicleLookup when tax or sorn cookie data is missing" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())

      val result = completeAndConfirm.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "not call the micro service when the date of acquisition is before the date of disposal and return a bad request" in new WithApplication {
      // The date of acquisition is 19-10-2012
      val disposalDate = DateTime.parse("20-10-2012", DateTimeFormat.forPattern("dd-MM-yyyy"))

      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel(keeperEndDate = Some(disposalDate)))
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val acquireServiceMock = mock[AcquireService]
      val completeAndConfirm = acquireController(acquireServiceMock)

      val result = completeAndConfirm.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
        verify(acquireServiceMock, never()).invoke(any[AcquireRequestDto], any[TrackingId])
      }
    }

    "call the micro service when the date of acquisition is after the date of disposal and redirect to the next page" in new WithApplication {
      // The date of acquisition is 19-10-2012
      val disposalDate = DateTime.parse("18-10-2012", DateTimeFormat.forPattern("dd-MM-yyyy"))

      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel(keeperEndDate = Some(disposalDate)))
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val acquireServiceMock = mock[AcquireService]
      val completeAndConfirm = acquireController(acquireServiceMock)

      when(acquireServiceMock.invoke(any[AcquireRequestDto], any[TrackingId]))
        .thenReturn(Future.successful {
        (OK, Some(acquireResponseSuccess))
      })

      val result = completeAndConfirm.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(AcquireSuccessPage.address))
        verify(acquireServiceMock, times(1)).invoke(any[AcquireRequestDto], any[TrackingId])
      }
    }

    "call the micro service when the date of acquisition is the same as the date of disposal and redirect to the next page" in new WithApplication {
      // The date of acquisition is 19-10-2012
      val disposalDate = DateTime.parse("19-10-2012", DateTimeFormat.forPattern("dd-MM-yyyy"))

      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel(keeperEndDate = Some(disposalDate)))
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

      val acquireServiceMock = mock[AcquireService]
      val completeAndConfirm = acquireController(acquireServiceMock)

      when(acquireServiceMock.invoke(any[AcquireRequestDto], any[TrackingId]))
        .thenReturn(Future.successful {
        (OK, Some(acquireResponseSuccess))
      })

      val result = completeAndConfirm.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(AcquireSuccessPage.address))
        verify(acquireServiceMock, times(1)).invoke(any[AcquireRequestDto], any[TrackingId])
      }
    }

    "redirect to next page when all fields are complete for new keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())

      val acquireSuccess = acquireController(acquireWebService =
        acquireWebService(acquireServiceResponse = Some(acquireResponseApplicationBeingProcessed)))

      val result = acquireSuccess.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(AcquireSuccessPage.address))
      }
    }

    "replace numeric mileage error message for with standard error message" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(mileage = "$$")
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())

      val result = completeAndConfirm.submitWithDateCheck(request)
      val replacementMileageErrorMessage = "You must enter a valid mileage between 0 and 999999"
      replacementMileageErrorMessage.r.findAllIn(contentAsString(result)).length should equal(2)
    }

    "redirect to next page when mandatory fields are complete for new keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
        .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())

      val acquireSuccess = acquireController(acquireWebService =
        acquireWebService(acquireServiceResponse = Some(acquireResponseApplicationBeingProcessed)))

      val result = acquireSuccess.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(AcquireSuccessPage.address))
      }
    }

    "return a bad request if consent is not ticked" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(consent = "")
        .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
        .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())

      val result = completeAndConfirm.submitWithDateCheck(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "Render dates in the correct timezone" in new WithApplication {
      timeZoneFixture {
        val disposalDate = DateTime.parse("2015-04-02T00:00.000+01:00")

        val request = buildCorrectlyPopulatedRequest()
          .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())
          .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
          .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel(keeperEndDate = Some(disposalDate)))
          .withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
          .withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
          .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())

        val result = completeAndConfirm.submitWithDateCheck(request)
        contentAsString(result) should include("02/04/2015")
      }
    }
  }

  private def timeZoneFixture(test: => Unit): Unit = {
    val defaultJodaTimeZone = DateTimeZone.getDefault
    val defaultTimeZone = TimeZone.getDefault
    try {
      DateTimeZone.setDefault(DateTimeZone.forID("Europe/London"))
      TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))
      test
    } finally {
      DateTimeZone.setDefault(defaultJodaTimeZone)
      TimeZone.setDefault(defaultTimeZone)
    }
  }

  private def acquireWebService(acquireServiceStatus: Int = OK,
                                acquireServiceResponse: Option[AcquireResponseDto] = Some(acquireResponseSuccess)): AcquireWebService = {
    val acquireWebService = mock[AcquireWebService]
    when(acquireWebService.callAcquireService(any[AcquireRequestDto], any[TrackingId])).
      thenReturn(Future.successful {
      val fakeJson = acquireServiceResponse map (Json.toJson(_))
      new FakeResponse(status = acquireServiceStatus, fakeJson = fakeJson) // Any call to a webservice will always return this successful response.
    })

    acquireWebService
  }

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
    when(config.googleAnalyticsTrackingId).thenReturn(Some("trackingId"))
    when(config.acquire).thenReturn(new AcquireConfig)
    when(config.assetsUrl).thenReturn(None)
    config
  }

  private def acquireController(acquireWebService: AcquireWebService): CompleteAndConfirm = {
    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] = invocation.getArguments()(1).asInstanceOf[Future[_]]
    })
    val acquireService = new AcquireServiceImpl(config.acquire, acquireWebService, healthStatsMock)
    acquireController(acquireService)
  }

  private def acquireController(acquireService: AcquireService)
                               (implicit config: Config = config,
                                dateService: DateService = dateServiceStubbed()): CompleteAndConfirm = {
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])

    val emailServiceMock: EmailService = mock[EmailService]
    when(emailServiceMock.invoke(any[EmailServiceSendRequest](), any[TrackingId])).
      thenReturn(Future(EmailServiceSendResponse()))

    new CompleteAndConfirm(acquireService, emailServiceMock)
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
    injector.getInstance(classOf[CompleteAndConfirm])
  }

  private lazy val present = {
    val request = FakeRequest()
      .withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
      .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      .withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
      .withCookies(CookieFactoryForUnitSpecs.allowGoingToCompleteAndConfirm())

    completeAndConfirm.present(request)
  }
}