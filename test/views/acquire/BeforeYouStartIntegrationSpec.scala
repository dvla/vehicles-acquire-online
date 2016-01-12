package views.acquire

import composition.TestHarness
import helpers.tags.UiTag
import helpers.UiSpec
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.{SetupTradeDetailsPage, BeforeYouStartPage}
import pages.acquire.BeforeYouStartPage.startNow
import pages.common.Feedback.AcquireEmailFeedbackLink


class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      pageTitle should equal(BeforeYouStartPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

  }

  "startNow button" should {
    "go to next page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      click on startNow
      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }
}