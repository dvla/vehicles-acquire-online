package controllers.acquire

import com.tzavellas.sse.guice.ScalaModule
import controllers.VehicleLookup
import helpers.UnitSpec
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.mappings.DocumentReferenceNumber
import common.webserviceclients.vehiclelookup.{VehicleDetailsRequestDto, VehicleDetailsResponseDto, VehicleLookupServiceImpl, VehicleLookupWebService}
import utils.helpers.Config
import viewmodels.VehicleLookupFormViewModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId, VehicleSoldToId}
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid, TraderBusinessNameValid}
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import webserviceclients.fakes.FakeResponse
import webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid, vehicleDetailsResponseSuccess}
import play.api.libs.ws.WSResponse
import views.acquire.VehicleLookup.VehicleSoldTo_Private

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import helpers.acquire.CookieFactoryForUnitSpecs
import helpers.WithApplication
import pages.acquire.{PrivateKeeperDetailsPage, SetupTradeDetailsPage, BusinessChooseYourAddressPage}

final class VehicleLookupUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      present.futureValue.header.status should equal(play.api.http.Status.OK)
    }

    "redirect to setupTradeDetails page when user has not set up a trader for disposal" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator().present(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleLookupFormModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should include(ReferenceNumberValid)
      content should include(RegistrationNumberValid)
    }

    "display data captured in previous pages" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)

      content should include(TraderBusinessNameValid)
      content should include(BuildingNameOrNumberValid)
      content should include(Line2Valid)
      content should include(Line3Valid)
      content should include(PostTownValid)
      content should include(webserviceclients.fakes.FakeAddressLookupService.PostcodeValid)
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().present(request)
      val content = contentAsString(result)
      content should not include ReferenceNumberValid
      content should not include RegistrationNumberValid
    }
  }

  "submit" should {
    "replace max length error message for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "1" * (DocumentReferenceNumber.MaxLength + 1)).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace required and min length error messages for document reference number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(referenceNumber = "").
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      // check the validation summary text
      "Document reference number - Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
      // check the form item validation
      "\"error\">Document reference number must be an 11-digit number".
        r.findAllIn(contentAsString(result)).length should equal(1)
    }

    "replace max length error message for vehicle registration number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "PJ05YYYX").
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      val count = "Must be as shown on the latest V5C".r.findAllIn(contentAsString(result)).length

      count should equal(2)
    }

    "replace required and min length error messages for vehicle registration number with standard error message (US43)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(registrationNumber = "").
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
      val result = vehicleLookupResponseGenerator().submit(request)
      val count = "Must be as shown on the latest V5C".r.findAllIn(contentAsString(result)).length

      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of the page and once above the field.
    }

    "redirect to BusinessChooseYourAddress when back button is pressed and there is a uprn" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(uprn = Some(traderUprnValid)))
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
    }

    "redirect to SetupTradeDetails page when back button is pressed and dealer details is not in cache" in new WithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "redirect to SetUpTradeDetails when back button and the user has completed the vehicle lookup form" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(uprn = Some(traderUprnValid)))
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
    }

    "redirect to SetUpTradeDetails when back button clicked and there are no trader details stored in cache" in new WithApplication {
      // No cache setup with dealer details
      val request = buildCorrectlyPopulatedRequest()
      val result = vehicleLookupResponseGenerator().back(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
    }

    "redirect to PrivateKeeperDetails when submit button clicked and Private Individual is selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(ReferenceNumberValid, RegistrationNumberValid, VehicleSoldTo_Private).
        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(uprn = Some(traderUprnValid)))
      val result = vehicleLookupResponseGenerator().submit(request)

      result.futureValue.header.headers.get(LOCATION) should equal(Some(PrivateKeeperDetailsPage.address))
    }

//    TODO : Resolve this failing test, currently redirects to private rather than business
//    "redirect to BusinessKeeperDetails when submit button clicked and Business is selected" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest(ReferenceNumberValid, RegistrationNumberValid, VehicleSoldTo_Business).
//        withCookies(CookieFactoryForUnitSpecs.traderDetailsModel(uprn = Some(traderUprnValid)))
//      val result = vehicleLookupResponseGenerator().submit(request)
//
//      result.futureValue.header.headers.get(LOCATION) should equal(Some(BusinessKeeperDetailsPage.address))
//    }
  }

  private val ExitAnchorHtml = """a id="exit""""

  private def responseThrows: Future[WSResponse] = Future {
    throw new RuntimeException("This error is generated deliberately by a test")
  }

  private def vehicleLookupResponseGenerator(fullResponse: (Int, Option[VehicleDetailsResponseDto]) = vehicleDetailsResponseSuccess) = {
    val (status, vehicleDetailsResponse) = fullResponse
    val ws: VehicleLookupWebService = mock[VehicleLookupWebService]
    when(ws.callVehicleLookupService(any[VehicleDetailsRequestDto], any[String])).thenReturn(Future {
      val responseAsJson: Option[JsValue] = vehicleDetailsResponse match {
        case Some(e) => Some(Json.toJson(e))
        case _ => None
      }
      new FakeResponse(status = status, fakeJson = responseAsJson) // Any call to a webservice will always return this successful response.
    })
    val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(ws)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    new VehicleLookup(vehicleLookupServiceImpl)
  }

  private lazy val vehicleLookupError = {
    val permitted = true // The lookup is permitted as we want to test failure on the vehicle lookup micro-service step.
    val vehicleLookupWebService: VehicleLookupWebService = mock[VehicleLookupWebService]
    when(vehicleLookupWebService.callVehicleLookupService(any[VehicleDetailsRequestDto], any[String])).thenReturn(Future {
      throw new IllegalArgumentException
    })
    val vehicleLookupServiceImpl = new VehicleLookupServiceImpl(vehicleLookupWebService)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    val vehiclesLookup = new VehicleLookup(vehicleLookupServiceImpl)
  }

  private def buildCorrectlyPopulatedRequest(referenceNumber: String = ReferenceNumberValid,
                                             registrationNumber: String = RegistrationNumberValid,
                                             soldTo: String = VehicleSoldTo_Private
                                              ) = {
    FakeRequest().withFormUrlEncodedBody(
      DocumentReferenceNumberId -> referenceNumber,
      VehicleRegistrationNumberId -> registrationNumber,
      VehicleSoldToId -> soldTo)
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.traderDetailsModel())
    vehicleLookupResponseGenerator(vehicleDetailsResponseSuccess).present(request)
  }

  private def lookupWithMockConfig(config: Config): VehicleLookup =
    testInjector(new ScalaModule() {
      override def configure(): Unit = bind[Config].toInstance(config)
    }).getInstance(classOf[VehicleLookup])

  private val testDuration = 7.days.toMillis
}
