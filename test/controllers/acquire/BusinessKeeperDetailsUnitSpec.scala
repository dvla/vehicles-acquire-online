package controllers.acquire

import pages.acquire.SetupTradeDetailsPage
import play.api.test.WithApplication
import controllers.acquire.Common.PrototypeHtml
import helpers.acquire.CookieFactoryForUnitSpecs
import controllers.BusinessKeeperDetails
import helpers.UnitSpec
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import viewmodels.BusinessKeeperDetailsFormViewModel.Form.{FleetNumberId, BusinessNameId, EmailId}
import pages.acquire.BusinessKeeperDetailsPage.{FleetNumberValid, BusinessNameValid, EmailValid}

class BusinessKeeperDetailsUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) {
        r => r.header.status should equal(OK)
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

      val controller = new BusinessKeeperDetails()
      val result = controller.present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
      val result = businessKeeperDetails.present(request)
      val content = contentAsString(result)
      content should include(s"""value="$FleetNumberValid"""")
      content should include(s"""value="$BusinessNameValid"""")
      content should include(s"""value="$EmailValid"""")
    }

    "redirect to setup trade details when no cookie is present" in new WithApplication {
      val request = buildRequest()
      val result = businessKeeperDetails.present(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }
  }

  "submit" should {
    "redirect to next page when only mandatory fields are filled in" in new WithApplication {
      val request = buildRequest(fleetNumber = "", email = "")
        .withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = businessKeeperDetails.submit(request)
      whenReady(result) {
        r =>
//          r.header.headers.get(LOCATION) should equal(Some("vrm-acquire/select-keeper-address")) //ToDo amend when next page implemented
          r.header.headers.get(LOCATION) should equal(None) //ToDo amend when next page implemented
      }
    }

    "redirect to next page when all fields are complete" in new WithApplication {
      val request = buildRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = businessKeeperDetails.submit(request)
      whenReady(result) {
        r =>
//          r.header.headers.get(LOCATION) should equal(Some("vrm-acquire/select-keeper-address")) //ToDo amend when next page implemented
          r.header.headers.get(LOCATION) should equal(None) //ToDo amend when next page implemented
      }
    }

    "redirect to setup trade details when no cookie is present with invalid submission" in new WithApplication {
      val request = buildRequest(fleetNumber = "-12345")
      val result = businessKeeperDetails.submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "return a bad request if no details are entered" in new WithApplication {
      val request = buildRequest(businessName = "")
        .withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = businessKeeperDetails.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "replace required error message for business name with standard error message " in new WithApplication {
      val request = buildRequest(businessName = "")
        .withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
      val result = businessKeeperDetails.submit(request)
      val errorMessage = "Must be between two and 56 characters and only contain valid characters"
      val count = errorMessage.r.findAllIn(contentAsString(result)).length
      count should equal(2)
    }
  }

  private def buildRequest(fleetNumber: String = FleetNumberValid,
                           businessName: String = BusinessNameValid,
                           email: String = EmailValid) = {
    FakeRequest().withFormUrlEncodedBody(
      FleetNumberId -> fleetNumber,
      BusinessNameId -> businessName,
      EmailId -> email
    )
  }

  private val businessKeeperDetails = {
    injector.getInstance(classOf[BusinessKeeperDetails])
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.vehicleDetailsModel())
    businessKeeperDetails.present(request)
  }
}