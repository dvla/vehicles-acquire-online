package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import helpers.common.ProgressBar
import helpers.tags.UiTag
import helpers.UiSpec
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.AcquireFailurePage.buyAnother
import pages.acquire.{AcquireFailurePage, BeforeYouStartPage, VehicleLookupPage}

class AcquireFailureIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireFailurePage
      pageTitle should equal(AcquireFailurePage.title)
    }

    "redirect to before you start if cache is empty on page load" taggedAs UiTag in new WebBrowserForSelenium {
      go to AcquireFailurePage
      pageTitle should equal(BeforeYouStartPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to AcquireFailurePage
      pageSource should not contain ProgressBar.div
    }
  }

  "vehiclelookup button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireFailurePage
      click on buyAnother
      pageTitle should equal(VehicleLookupPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleAndKeeperDetails().
      dealerDetails().
      newKeeperDetails().
      completeAndConfirmDetails().
      vehicleTaxOrSornFormModel().
      completeAndConfirmResponseModelModel()
}