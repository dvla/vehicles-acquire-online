package views.acquire

import helpers.common.ProgressBar
import ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import pages.acquire.{SetupTradeDetailsPage, BeforeYouStartPage}
import pages.acquire.BeforeYouStartPage.startNow

final class SetupTradeDetailsIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      click on startNow

      page.title should equal(SetupTradeDetailsPage.title)
    }
  }
}