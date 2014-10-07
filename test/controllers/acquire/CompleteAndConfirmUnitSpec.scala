package controllers.acquire

import controllers.{PrivateKeeperDetails, CompleteAndConfirm}
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import play.api.test.Helpers.{LOCATION, BAD_REQUEST, OK, contentAsString, defaultAwaitTimeout}
import play.api.test.{FakeRequest, WithApplication}
import controllers.acquire.Common.PrototypeHtml
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import pages.acquire.CompleteAndConfirmPage.{MileageValid, ConsentTrue}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import utils.helpers.Config
import org.mockito.Mockito.when
import pages.acquire.{VehicleLookupPage, SetupTradeDetailsPage}
import models.CompleteAndConfirmFormModel.Form.{MileageId, DateOfSaleId, ConsentId}
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{DayId, MonthId, YearId}

class CompleteAndConfirmUnitSpec extends UnitSpec {

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

    "present a full form when privateKeeperDetailsComplete cookie is present" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
      val content = contentAsString(newKeeperDetailsComplete.present(request))

      content should include(MileageValid)
    }

    "display empty fields when privateKeeperDetailsComplete cookie does not exist" in new WithApplication {
      val request = FakeRequest()
      val result = newKeeperDetailsComplete.present(request)
      val content = contentAsString(result)
      content should not include MileageValid
    }

    "redirect to vehicle lookup when no new keeper details cookie are present" in new WithApplication {
      val request = FakeRequest()
      val result = newKeeperDetailsComplete.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  "submit" should {
    "replace numeric mileage error message for with standard error message " in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(mileage = "$$")
      val result = newKeeperDetailsComplete.submit(request)
      val count = "You must enter a valid mileage between 0 and 999999".
        r.findAllIn(contentAsString(result)).length
      count should equal(2)
    }

    "redirect to next page when mandatory fields are complete" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()

      val result = newKeeperDetailsComplete.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal (Some("/vrm-acquire/not-implemented")) //ToDo - update when next section is implemented
      }
    }

    "redirect to next page when all fields are complete" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()

      val result = newKeeperDetailsComplete.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal (Some("/vrm-acquire/not-implemented")) //ToDo - update when next section is implemented
      }
    }

    "return a bad request if consent is not ticked" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(consent="")
      val result = newKeeperDetailsComplete.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }
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

  private val newKeeperDetailsComplete = {
    injector.getInstance(classOf[CompleteAndConfirm])
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
    newKeeperDetailsComplete.present(request)
  }
}
