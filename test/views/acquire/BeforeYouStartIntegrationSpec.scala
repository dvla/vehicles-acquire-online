package views.acquire

import composition.TestHarness
import helpers.common.ProgressBar
import helpers.tags.UiTag
import helpers.UiSpec
import pages.acquire.{SetupTradeDetailsPage, BeforeYouStartPage}
import pages.acquire.BeforeYouStartPage.startNow
import pages.common.Feedback.AcquireEmailFeedbackLink
import ProgressBar.progressStep

class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      page.title should equal(BeforeYouStartPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      page.source.contains(progressStep(1)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      page.source.contains(progressStep(1)) should equal(false)
    }
  }

  "startNow button" should {
    "go to next page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      click on startNow
      page.title should equal(SetupTradeDetailsPage.title)
    }
  }
}