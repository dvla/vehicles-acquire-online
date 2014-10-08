package controllers.acquire

import controllers.{PrivateKeeperDetails, CompleteAndConfirm}
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import play.api.test.Helpers.{LOCATION, BAD_REQUEST, OK, contentAsString, defaultAwaitTimeout}
import play.api.test.{FakeRequest, WithApplication}
import controllers.acquire.Common.PrototypeHtml
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import pages.acquire.buildAppUrl
import pages.acquire.CompleteAndConfirmPage.{MileageValid, ConsentTrue}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import utils.helpers.Config
import org.mockito.Mockito.when
import pages.acquire.VehicleLookupPage
import models.CompleteAndConfirmFormModel.Form.{MileageId, DateOfSaleId, ConsentId}
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{DayId, MonthId, YearId}

class CompleteAndConfirmUnitSpec extends UnitSpec {

  "present" should {
    "display the page with new private keeper cached" in new WithApplication {
      whenReady(presentWithNewPrivateKeeper) { r =>
        r.header.status should equal(OK)
      }
    }

    "display the page with new business keeper cached" in new WithApplication {
      whenReady(presentWithNewBusinessKeeper) { r =>
        r.header.status should equal(OK)
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(presentWithNewPrivateKeeper) should include(PrototypeHtml)
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

    "present a full form when new keeper cookie is present for new private keeper" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
      val content = contentAsString(completeAndConfirm.present(request))
      content should include(MileageValid)
    }

    "present a full form when new keeper cookie is present for new business keeper" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.completeAndConfirmModel())
      val content = contentAsString(completeAndConfirm.present(request))
      content should include(MileageValid)
    }

    "display empty fields when new keeper complete cookie does not exist" in new WithApplication {
      val request = FakeRequest()
      val result = completeAndConfirm.present(request)
      val content = contentAsString(result)
      content should not include MileageValid
    }

    "redirect to vehicle lookup when no new keeper details cookie are present" in new WithApplication {
      val request = FakeRequest()
      val result = completeAndConfirm.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }
  }

  final val notImplementedUrl = buildAppUrl("not-implemented")
  final val replacementMileageErrorMessage = "You must enter a valid mileage between 0 and 999999"

  "submit" should {
    "replace numeric mileage error message for with standard error message" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(mileage = "$$").
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())

      val result = completeAndConfirm.submit(request)
      replacementMileageErrorMessage.r.findAllIn(contentAsString(result)).length should equal(2)
    }

    "redirect to next page when mandatory fields are complete for private keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())

      val result = completeAndConfirm.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal (Some(notImplementedUrl)) //ToDo - update when next section is implemented
      }
    }

    "redirect to next page when mandatory fields are complete for business keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())

      val result = completeAndConfirm.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal (Some(notImplementedUrl)) //ToDo - update when next section is implemented
      }
    }


    "redirect to next page when all fields are complete for private keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())

      val result = completeAndConfirm.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal (Some(notImplementedUrl)) //ToDo - update when next section is implemented
      }
    }

    "redirect to next page when all fields are complete for business keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())

      val result = completeAndConfirm.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal (Some(notImplementedUrl)) //ToDo - update when next section is implemented
      }
    }

    "return a bad request if consent is not ticked" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(consent="").
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())

      val result = completeAndConfirm.submit(request)
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

  private val completeAndConfirm = {
    injector.getInstance(classOf[CompleteAndConfirm])
  }

  private lazy val presentWithNewPrivateKeeper = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
    completeAndConfirm.present(request)
  }

  private lazy val presentWithNewBusinessKeeper = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
    completeAndConfirm.present(request)
  }
}