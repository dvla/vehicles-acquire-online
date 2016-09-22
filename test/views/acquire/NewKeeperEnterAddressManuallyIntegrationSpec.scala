package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.NewKeeperEnterAddressManuallyPage.{happyPath, happyPathMandatoryFieldsOnly, sadPath}
import pages.acquire.{BeforeYouStartPage, NewKeeperEnterAddressManuallyPage, VehicleTaxOrSornPage}
import pages.common.ErrorPanel
import pages.common.Feedback.AcquireEmailFeedbackLink
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction

class NewKeeperEnterAddressManuallyIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to NewKeeperEnterAddressManuallyPage
      pageTitle should equal(NewKeeperEnterAddressManuallyPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to NewKeeperEnterAddressManuallyPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to NewKeeperEnterAddressManuallyPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "next button" should {
    "accept and redirect when all fields are input with valid entry" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath()
      pageTitle should equal(VehicleTaxOrSornPage.title)
    }

    "accept when only mandatory fields only are input" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      happyPathMandatoryFieldsOnly()
      pageTitle should equal(VehicleTaxOrSornPage.title)
    }

    "display validation error messages when no details are entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      sadPath
      ErrorPanel.numberOfErrors should equal(3)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .vehicleAndKeeperDetails()
      .businessKeeperDetails() // Not bothering with private keeper details
}
