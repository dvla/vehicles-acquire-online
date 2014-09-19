package controllers.acquire

import controllers.{BusinessKeeperDetailsComplete, PrivateKeeperDetails}
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import models.PrivateKeeperDetailsCompleteFormModel.Form._
import pages.acquire.PrivateKeeperDetailsCompletePage._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import controllers.acquire.Common.PrototypeHtml
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import pages.acquire.BusinessKeeperDetailsCompletePage.MileageValid
import utils.helpers.Config
import org.mockito.Mockito.when
import pages.acquire.SetupTradeDetailsPage
import models.BusinessKeeperDetailsCompleteFormModel.Form.MileageId

class BusinessKeeperDetailsCompleteUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
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

    "present a full form when businessKeeperDetailsComplete cookie is present" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsCompleteModel())
      val content = contentAsString(businessKeeperDetailsComplete.present(request))
      content should include(MileageValid)
    }

    "display empty fields when businessKeeperDetailsComplete cookie does not exist" in new WithApplication {
      val request = FakeRequest()
      val result = businessKeeperDetailsComplete.present(request)
      val content = contentAsString(result)
      content should not include MileageValid
    }

    "redirect to setuptrade details when no businesskeeperdetails cookie is present" in new WithApplication {
      val request = FakeRequest()
      val result = businessKeeperDetailsComplete.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }
  }

  "submit" should {
    "redirect to next page when mandatory fields are complete" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()

      val result = businessKeeperDetailsComplete.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal (Some("/vrm-acquire/not-implemented")) //ToDo - update when next section is implemented
      }
    }

    "redirect to next page when all fields are complete" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()

      val result = businessKeeperDetailsComplete.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal (Some("/vrm-acquire/not-implemented")) //ToDo - update when next section is implemented
      }
    }

    "return a bad request if consent is not ticked" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(consent="")
      val result = businessKeeperDetailsComplete.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }
    }

  private def buildCorrectlyPopulatedRequest(mileage: String = MileageValid,
                                             consent: String = ConsentTrue) = {
    FakeRequest().withFormUrlEncodedBody(MileageId -> mileage,
      ConsentId -> consent)
  }

  private val businessKeeperDetailsComplete = {
    injector.getInstance(classOf[BusinessKeeperDetailsComplete])
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
    businessKeeperDetailsComplete.present(request)
  }
}