package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.MicroServiceErrorPage.{exit, tryAgain}
import pages.acquire.{BeforeYouStartPage, MicroServiceErrorPage, SetupTradeDetailsPage, VehicleLookupPage}
import pages.common.AlternateLanguages.{cymraeg, english}
import pages.common.Feedback.AcquireEmailFeedbackLink
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

class MicroServiceErrorIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to MicroServiceErrorPage
      pageTitle should equal(MicroServiceErrorPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to MicroServiceErrorPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

  }

  "tryAgain button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to MicroServiceErrorPage
      click on tryAgain
      pageTitle should equal(VehicleLookupPage.title)
    }

    "redirect to setuptradedetails when no details are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to MicroServiceErrorPage
      click on tryAgain
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "not be its own referer" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to MicroServiceErrorPage
      click on cymraeg
      click on english
      click on tryAgain
      pageTitle should equal(VehicleLookupPage.title)
    }
  }

  "exit button" should {
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to MicroServiceErrorPage
      click on exit
      pageTitle should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails()
}
