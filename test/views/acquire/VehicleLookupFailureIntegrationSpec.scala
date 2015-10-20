package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import helpers.common.ProgressBar
import helpers.tags.UiTag
import helpers.UiSpec
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.VehicleLookupFailurePage.{beforeYouStart, vehicleLookup}
import pages.acquire.{BeforeYouStartPage, SetupTradeDetailsPage, VehicleLookupPage, VehicleLookupFailurePage}
import pages.common.Feedback.AcquireEmailFeedbackLink
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts

class VehicleLookupFailureIntegrationSpec extends UiSpec with TestHarness {
  val expectedString = "Only a limited number of attempts can be made to retrieve vehicle details " +
    "for each vehicle registration number entered."

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      pageTitle should equal(VehicleLookupFailurePage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      pageSource should not contain ProgressBar.div
    }

    "redirect to setuptrade details if cache is empty on page load" taggedAs UiTag in new WebBrowserForSelenium {
      go to VehicleLookupFailurePage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "redirect to setuptrade details if only VehicleLookupFormModelCache is populated" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.vehicleLookupFormModel()
      go to VehicleLookupFailurePage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "redirect to setuptrade details if only dealerDetails cache is populated" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.dealerDetails()
      go to VehicleLookupFailurePage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "display messages that show that the number of brute force attempts does not impact which messages are displayed when 1 attempt has been made" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      CookieFactoryForUISpecs.
        dealerDetails().
        bruteForcePreventionViewModel(attempts = 1, maxAttempts = MaxAttempts).
        vehicleLookupFormModel().
        vehicleLookupResponseCode(responseCode = "300L - vehicle_and_keeper_lookup_vrm_not_found")

      go to VehicleLookupFailurePage
      pageSource should include(expectedString)
    }

    "display messages that show that the number of brute force attempts does not impact which messages are displayed when 2 attempts have been made" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      CookieFactoryForUISpecs.
        dealerDetails().
        bruteForcePreventionViewModel(attempts = 2, maxAttempts = MaxAttempts).
        vehicleLookupFormModel().
        vehicleLookupResponseCode(responseCode = "400P - vehicle_and_keeper_lookup_vrm_not_found")

      go to VehicleLookupFailurePage
      pageSource should include(expectedString)
    }

    "display appropriate messages for document reference mismatch" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage

      CookieFactoryForUISpecs.
        dealerDetails().
        bruteForcePreventionViewModel().
        vehicleLookupFormModel().
        vehicleLookupResponseCode(responseCode = "600K - vehicle_and_keeper_lookup_document_reference_mismatch")

      go to VehicleLookupFailurePage
      pageSource should include(expectedString)
    }
  }

  "vehicleLookup button" should {
    "redirect to vehiclelookup when button clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      click on vehicleLookup
      pageTitle should equal(VehicleLookupPage.title)
    }
  }

  "beforeYouStart button" should {
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupFailurePage
      click on beforeYouStart
      pageTitle should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      dealerDetails().
      bruteForcePreventionViewModel().
      vehicleLookupFormModel().
      vehicleLookupResponseCode()
}