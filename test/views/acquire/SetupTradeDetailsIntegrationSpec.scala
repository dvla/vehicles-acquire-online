package views.acquire

import composition.TestHarness
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.SetupTradeDetailsPage.{happyPath, PostcodeValid, TraderBusinessNameValid}
import pages.acquire.SetupTradeDetailsPage
import pages.common.{Accessibility, ErrorPanel}
import pages.common.Feedback.AcquireEmailFeedbackLink
import uk.gov.dvla.vehicles.presentation.common.model.SetupTradeDetailsFormModel
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}

class SetupTradeDetailsIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to SetupTradeDetailsPage
      pageTitle should equal(SetupTradeDetailsPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to SetupTradeDetailsPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }
  }

  "lookup button" should {
    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath()
      pageTitle should equal("Select trader address")
    }

    "display one summary validation error message when no postcode is entered" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessName = TraderBusinessNameValid, traderBusinessPostcode = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one summary validation error message when no trader business name is entered" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessName = "", traderBusinessPostcode = PostcodeValid)
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display two summary validation error messages when no details are entered" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessName = "", traderBusinessPostcode = "")
      ErrorPanel.numberOfErrors should equal(2)
    }

    "display one summary validation error message when an incorrectly email is entered" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessEmail = Some("email_with_no_at_symbol"))
      ErrorPanel.numberOfErrors should equal(1)
    }

    "add aria required attribute to trader name field when required field not input" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessName = "")
      Accessibility.ariaRequiredPresent(SetupTradeDetailsFormModel.Form.TraderNameId) should equal(true)
    }

    "add aria invalid attribute to trader name field when required field not input" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessName = "")
      Accessibility.ariaInvalidPresent(SetupTradeDetailsFormModel.Form.TraderNameId) should equal(true)
    }

    "add aria required attribute to trader postcode field when required field not input" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessPostcode = "")
      Accessibility.ariaRequiredPresent(SetupTradeDetailsFormModel.Form.TraderPostcodeId) should equal(true)
    }

    "add aria invalid attribute to trader postcode field when required field not input" taggedAs UiTag in new WebBrowserForSelenium {
      happyPath(traderBusinessPostcode = "")
      Accessibility.ariaInvalidPresent(SetupTradeDetailsFormModel.Form.TraderPostcodeId) should equal(true)
    }
  }
}
