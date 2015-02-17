package controllers.acquire

import controllers.PrivateKeeperDetails
import controllers.acquire.Common.PrototypeHtml
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import org.mockito.Mockito.when
import pages.acquire.{NewKeeperChooseYourAddressPage, SetupTradeDetailsPage}
import pages.acquire.PrivateKeeperDetailsPage.DayDateOfBirthValid
import pages.acquire.PrivateKeeperDetailsPage.MonthDateOfBirthValid
import pages.acquire.PrivateKeeperDetailsPage.YearDateOfBirthValid
import pages.acquire.PrivateKeeperDetailsPage.EmailValid
import pages.acquire.PrivateKeeperDetailsPage.FirstNameValid
import pages.acquire.PrivateKeeperDetailsPage.LastNameValid
import pages.acquire.PrivateKeeperDetailsPage.DriverNumberValid
import pages.acquire.PrivateKeeperDetailsPage.PostcodeValid
import play.api.i18n.Messages
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.mappings.{TitleType, TitlePickerString}
import common.model.PrivateKeeperDetailsFormModel.Form.PostcodeId
import common.model.PrivateKeeperDetailsFormModel.Form.EmailId
import common.model.PrivateKeeperDetailsFormModel.Form.FirstNameId
import common.model.PrivateKeeperDetailsFormModel.Form.LastNameId
import common.model.PrivateKeeperDetailsFormModel.Form.TitleId
import common.model.PrivateKeeperDetailsFormModel.Form.DriverNumberId
import common.services.DateService
import TitlePickerString.standardOptions
import utils.helpers.Config

class PrivateKeeperDetailsUnitSpec extends UnitSpec {

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
      implicit val config = mock[Config]
      implicit val dateService = injector.getInstance(classOf[DateService])
      when(config.isPrototypeBannerVisible).thenReturn(false)

      val privateKeeperDetailsPrototypeNotVisible = new PrivateKeeperDetails()
      val result = privateKeeperDetailsPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = privateKeeperDetails.present(request)
      val content = contentAsString(result)
      content should include(Messages(standardOptions(0)))
      content should include(FirstNameValid)
      content should include(LastNameValid)
      content should include(DayDateOfBirthValid)
      content should include(MonthDateOfBirthValid)
      content should include(YearDateOfBirthValid)
      content should include(EmailValid)
    }

    "display populated other title when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel(title = TitleType(4, "otherTitle")))
      val result = privateKeeperDetails.present(request)
      val content = contentAsString(result)
      content should include("otherTitle")
    }

    "display empty fields when cookie does not exist" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = privateKeeperDetails.present(request)
      val content = contentAsString(result)
      content should include(Messages(standardOptions(0)))
      content should not include "selected"
    }
  }

  "submit" should {
    "redirect to next page when mandatory fields are complete" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(email = "")
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = privateKeeperDetails.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(NewKeeperChooseYourAddressPage.address))
      }
    }

    "redirect to next page when all fields are complete" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = privateKeeperDetails.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(NewKeeperChooseYourAddressPage.address))
      }
    }

    "redirect to setup trade details when no cookie is present" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(title = "2")
      val result = privateKeeperDetails.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "return a bad request if no details are entered" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(title = "",
                                                   firstName = "",
                                                   lastName = "",
                                                   email = "",
                                                   driverNumber = "",
                                                   postcode = "")
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = privateKeeperDetails.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "replace required error message for postcode with standard error message" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(postcode = "")
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = privateKeeperDetails.submit(request)
      val errorMessage = "Must be between five and eight characters and in a valid format, e.g. AB1 2BA or AB12BA"
      val count = errorMessage.r.findAllIn(contentAsString(result)).length
      count should equal(2)
    }
  }

  private def buildCorrectlyPopulatedRequest(title: String = "1",
                                             firstName: String = FirstNameValid,
                                             lastName: String = LastNameValid,
                                             email: String = EmailValid,
                                             driverNumber: String = DriverNumberValid,
                                             postcode: String = PostcodeValid) = {
    FakeRequest().withFormUrlEncodedBody(
      s"$TitleId.${TitlePickerString.TitleRadioKey}" -> title,
      FirstNameId -> firstName,
      LastNameId -> lastName,
      EmailId -> email,
      DriverNumberId -> driverNumber,
      PostcodeId -> postcode
    )
  }

  private lazy val privateKeeperDetails = {
    injector.getInstance(classOf[PrivateKeeperDetails])
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    privateKeeperDetails.present(request)
  }
}