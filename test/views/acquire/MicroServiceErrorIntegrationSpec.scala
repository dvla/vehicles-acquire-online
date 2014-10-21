package views.acquire

import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.acquire.MicroServiceErrorPage.{exit, tryAgain}
import pages.acquire.{BeforeYouStartPage, MicroServiceErrorPage, SetupTradeDetailsPage, VehicleLookupPage}
import pages.common.Feedback.AcquireEmailFeedbackLink

final class MicroServiceErrorIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to MicroServiceErrorPage
      page.title should equal(MicroServiceErrorPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowser {
      go to MicroServiceErrorPage
      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to MicroServiceErrorPage
      page.source should not contain ProgressBar.div
    }
  }

  "tryAgain button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to MicroServiceErrorPage
      click on tryAgain
      page.title should equal(VehicleLookupPage.title)
    }

    "redirect to setuptradedetails when no details are cached" taggedAs UiTag in new WebBrowser {
      go to MicroServiceErrorPage
      click on tryAgain
      page.title should equal(SetupTradeDetailsPage.title)
    }
  }

  "exit button" should {
    "redirect to beforeyoustart" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to MicroServiceErrorPage
      click on exit
      page.title should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails()
}