package views.acquire

import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.WebDriver
import pages.acquire.AcquireFailurePage.buyAnother
import pages.acquire.{AcquireFailurePage, BeforeYouStartPage, VehicleLookupPage}

final class AcquireFailureIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireFailurePage
      page.title should equal(AcquireFailurePage.title)
    }

    "redirect to before you start if cache is empty on page load" taggedAs UiTag in new WebBrowser {
      go to AcquireFailurePage
      page.title should equal(BeforeYouStartPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to AcquireFailurePage
      page.source should not contain ProgressBar.div
    }
  }

  "vehiclelookup button" should {
    "redirect to vehiclelookup" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireFailurePage
      click on buyAnother
      page.title should equal(VehicleLookupPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      vehicleDetails().
      dealerDetails().
      newKeeperDetails().
      completeAndConfirmDetails().
      vehicleTaxOrSornFormModel().
      completeAndConfirmResponseModelModel()
}