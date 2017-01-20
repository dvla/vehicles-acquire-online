package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import org.openqa.selenium.{By, WebElement, WebDriver}
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.common.ErrorPanel
import pages.acquire.BeforeYouStartPage
import pages.acquire.NewKeeperChooseYourAddressPage
import pages.acquire.PrivateKeeperDetailsPage
import pages.acquire.SetupTradeDetailsPage
import pages.acquire.VehicleLookupPage
import play.api.i18n.Messages
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}
import webserviceclients.fakes.FakeAddressLookupService.addressWithoutUprn
import pages.acquire.PrivateKeeperDetailsPage.{navigate, back}
import pages.acquire.PrivateKeeperDetailsPage.{EmailInvalid, FirstNameInvalid, LastNameInvalid, TitleInvalid}
import pages.acquire.PrivateKeeperDetailsPage.{DriverNumberInvalid, PostcodeInvalid}
import pages.common.Feedback.AcquireEmailFeedbackLink

final class PrivateKeeperDetailsIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to PrivateKeeperDetailsPage
      pageTitle should equal(PrivateKeeperDetailsPage.title)

      // Ensure the date of birth fields are on the page
      PrivateKeeperDetailsPage.yearDateOfBirthTextBox.text should equal("")
      PrivateKeeperDetailsPage.monthDateOfBirthTextBox.text should equal("")
      PrivateKeeperDetailsPage.dayDateOfBirthTextBox.text should equal("")
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to PrivateKeeperDetailsPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "Redirect when no vehicle details are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to PrivateKeeperDetailsPage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to PrivateKeeperDetailsPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "next button" should {
    "go to the appropriate next page when all private keeper details are entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate()
      pageTitle should equal(NewKeeperChooseYourAddressPage.title)
    }

    "go to the appropriate next page when mandatory private keeper details are entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(email = None)
      pageTitle should equal(NewKeeperChooseYourAddressPage.title)
    }

    "display one validation error message when no title is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(title = TitleInvalid)
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when an incorrect email is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(email = Some(EmailInvalid))
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when an incorrect driver number is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(driverNumber = DriverNumberInvalid)
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when no first name is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(firstName = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when first name is invalid" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(firstName = FirstNameInvalid)
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when no last name is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(lastName = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when last name is invalid" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(lastName = LastNameInvalid)
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when an incorrect postcode is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(postcode = PostcodeInvalid)
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when the title and the first name are longer then 26" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(title = "tenchartdd", firstName = "15characterssdyff")
      ErrorPanel.text should include(Messages("error.titlePlusFirstName.tooLong"))
    }
  }

  "back" should {
    "display previous page when back link is clicked with uprn present" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithoutUprn).
        vehicleAndKeeperDetails()

      go to PrivateKeeperDetailsPage
      click on back
      pageTitle should equal(VehicleLookupPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()
      .vehicleAndKeeperDetails()
}
