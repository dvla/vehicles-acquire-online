package views.acquire

import helpers.common.ProgressBar
import helpers.acquire.CookieFactoryForUISpecs
import ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.common.ErrorPanel
import pages.acquire.BeforeYouStartPage
import pages.acquire.SetupTradeDetailsPage
import pages.acquire.VehicleLookupPage
import pages.acquire.PrivateKeeperDetailsPage
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import webserviceclients.fakes.FakeAddressLookupService.addressWithUprn
import pages.acquire.PrivateKeeperDetailsPage.{happyPath, sadPath, back}
import pages.acquire.PrivateKeeperDetailsPage.{OptionValid, EmailValid, FirstNameValid, SurnameValid}
import pages.acquire.PrivateKeeperDetailsPage.{EmailInvalid, FirstNameInvalid, SurnameInvalid}

final class PrivateKeeperDetailsIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      go to PrivateKeeperDetailsPage

      page.title should equal(PrivateKeeperDetailsPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()

      go to PrivateKeeperDetailsPage

      page.source.contains(progressStep(5)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()

      go to PrivateKeeperDetailsPage

      page.source.contains(progressStep(5)) should equal(false)
    }

    "Redirect when no vehicle details are cached" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "next button" should {
    "go to the appropriate next page when all private keeper details are entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath()

      page.title should equal("Not implemented") //ToDo amend title once next page is implemented
    }

    "go to the appropriate next page when mandatory private keeper details are entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      happyPath(email = "")

      page.title should equal("Not implemented") //ToDo amend title once next page is implemented
    }

    "display one validation error message when no title is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      sadPath(email = EmailValid, firstName = FirstNameValid, surname = SurnameValid)

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when an incorrect email is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      sadPath(title = OptionValid, firstName = FirstNameValid, surname = SurnameValid, email = EmailInvalid)

      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when no firstname is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      sadPath(title = OptionValid, firstName = FirstNameInvalid, surname = SurnameValid, email = EmailInvalid)

      ErrorPanel.numberOfErrors should equal(2)
    }

    "display one validation error message when no surname is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()

      sadPath(title = OptionValid, firstName = SurnameInvalid, surname = SurnameValid, email = EmailInvalid)

      ErrorPanel.numberOfErrors should equal(2)
    }
  }


  "back" should {
    "display previous page when back link is clicked with uprn present" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn).
        vehicleDetails()

      go to PrivateKeeperDetailsPage

      click on back

      page.title should equal(VehicleLookupPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()
      .vehicleDetails()
}
