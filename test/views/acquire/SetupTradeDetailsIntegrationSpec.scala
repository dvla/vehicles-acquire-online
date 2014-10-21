package views.acquire

import helpers.UiSpec
import helpers.common.ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import pages.common.{Accessibility, ErrorPanel}
import pages.acquire.SetupTradeDetailsPage.{happyPath, PostcodeValid, TraderBusinessNameValid}
import pages.acquire.SetupTradeDetailsPage
import models.SetupTradeDetailsFormModel
import pages.common.Feedback.AcquireEmailFeedbackLink

final class SetupTradeDetailsIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to SetupTradeDetailsPage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowser {
      go to SetupTradeDetailsPage
      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to SetupTradeDetailsPage
      page.source.contains(progressStep(2)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to SetupTradeDetailsPage
      page.source.contains(progressStep(2)) should equal(false)
    }
  }

  "lookup button" should {
    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowser {
      happyPath()
      page.title should equal("Select trader address")
    }

    "display one summary validation error message when no postcode is entered" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessName = TraderBusinessNameValid, traderBusinessPostcode = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one summary validation error message when no trader business name is entered" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessName = "", traderBusinessPostcode = PostcodeValid)
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display two summary validation error messages when no details are entered" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessName = "", traderBusinessPostcode = "")
      ErrorPanel.numberOfErrors should equal(2)
    }

    "display one summary validation error message when an incorrectly email is entered" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessEmail = "email_with_no_at_symbol")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "add aria required attribute to trader name field when required field not input" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessName = "")
      Accessibility.ariaRequiredPresent(SetupTradeDetailsFormModel.Form.TraderNameId) should equal(true)
    }

    "add aria invalid attribute to trader name field when required field not input" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessName = "")
      Accessibility.ariaInvalidPresent(SetupTradeDetailsFormModel.Form.TraderNameId) should equal(true)
    }

    "add aria required attribute to trader postcode field when required field not input" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessPostcode = "")
      Accessibility.ariaRequiredPresent(SetupTradeDetailsFormModel.Form.TraderPostcodeId) should equal(true)
    }

    "add aria invalid attribute to trader postcode field when required field not input" taggedAs UiTag in new WebBrowser {
      happyPath(traderBusinessPostcode = "")
      Accessibility.ariaInvalidPresent(SetupTradeDetailsFormModel.Form.TraderPostcodeId) should equal(true)
    }
  }
}