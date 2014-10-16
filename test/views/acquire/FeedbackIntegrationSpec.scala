package views.acquire

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import pages.acquire.{SetupTradeDetailsPage, VehicleLookupPage, BeforeYouStartPage}

final class FeedbackIntegrationSpec extends UiSpec with TestHarness {
  "Feedback link" should {
    "contain feedback email facility with appropriate subject on before you start page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage

      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "contain feedback email facility with appropriate subject on setup trade details page" taggedAs UiTag in new WebBrowser {
      go to SetupTradeDetailsPage

      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "contain feedback email facility with appropriate subject on vehicle lookup page" taggedAs UiTag in new ProgressBarTrue {
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails()
      go to VehicleLookupPage

      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }
  }

  private final val AcquireEmailFeedbackLink = "<a id=\"feedback\" href=\"mailto:vm.feedback@digital.dvla.gov.uk?" +
    "Subject=Buy%20from%20the%20trade%20feedback\">"
}