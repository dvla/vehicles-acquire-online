package views.acquire

import composition.TestHarness
import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.tags.UiTag
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.{EnterAddressManuallyPage, BeforeYouStartPage, SetupTradeDetailsPage}
import pages.common.UprnNotFoundPage
import pages.common.UprnNotFoundPage.setupTradeDetails
import pages.common.Feedback.AcquireEmailFeedbackLink
import helpers.acquire.CookieFactoryForUISpecs
import pages.common.UprnNotFoundPage.manualAddress

final class UprnNotFoundIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to UprnNotFoundPage
      pageTitle should equal(UprnNotFoundPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to UprnNotFoundPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to UprnNotFoundPage
      pageSource should not contain ProgressBar.div
    }
  }

  "setupTradeDetails button" should {
    "go to setuptradedetails page" taggedAs UiTag in new WebBrowserForSelenium {
      go to UprnNotFoundPage
      click on setupTradeDetails
      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }

  "manualAddress button" should {
    "go to manualaddress page after the Manual Address button is clicked and trade details have been set up in cache" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.setupTradeDetails()
      go to UprnNotFoundPage

      click on manualAddress

      pageTitle should equal (EnterAddressManuallyPage.title)
    }

    "go to setuptradedetails page when trade details have not been set up in cache" taggedAs UiTag in new WebBrowserForSelenium {
      go to UprnNotFoundPage

      click on manualAddress

      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }
}