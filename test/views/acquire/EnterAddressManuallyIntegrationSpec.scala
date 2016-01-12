package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.EnterAddressManuallyPage.{happyPath, happyPathMandatoryFieldsOnly, sadPath}
import pages.acquire.{BeforeYouStartPage, EnterAddressManuallyPage, VehicleLookupPage}
import pages.common.ErrorPanel
import pages.common.Feedback.AcquireEmailFeedbackLink
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction

class EnterAddressManuallyIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to EnterAddressManuallyPage
      pageTitle should equal(EnterAddressManuallyPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to EnterAddressManuallyPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }


    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()

      go to EnterAddressManuallyPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }

    "not display certain labels when rendered with base template" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to EnterAddressManuallyPage
      pageSource should not contain "addressAndPostcode"
    }
  }

  "next button" should {
    "accept and redirect when all fields are input with valid entry" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath()
      pageTitle should equal(VehicleLookupPage.title)
    }

    "accept when only mandatory fields only are input" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      happyPathMandatoryFieldsOnly()
      pageTitle should equal(VehicleLookupPage.title)
    }

    "display validation error messages when no details are entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      sadPath
      ErrorPanel.numberOfErrors should equal(2)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.setupTradeDetails()
}