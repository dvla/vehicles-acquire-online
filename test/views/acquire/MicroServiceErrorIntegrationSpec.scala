package views.acquire

import composition.TestHarness
import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.MicroServiceErrorPage.{exit, tryAgain}
import pages.acquire.{BeforeYouStartPage, MicroServiceErrorPage, SetupTradeDetailsPage, VehicleLookupPage}
import pages.common.Feedback.AcquireEmailFeedbackLink

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

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to MicroServiceErrorPage
      pageSource should not contain ProgressBar.div
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