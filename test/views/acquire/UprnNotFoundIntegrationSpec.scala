package views.acquire

import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import pages.common.UprnNotFoundPage.{manualAddress, setupTradeDetails}
import pages.common.UprnNotFoundPage
import pages.acquire.SetupTradeDetailsPage

final class UprnNotFoundIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to UprnNotFoundPage

      page.title should equal(UprnNotFoundPage.title)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to UprnNotFoundPage

      page.source should not contain ProgressBar.div
    }
  }

  "setupTradeDetails button" should {
    "go to setuptradedetails page" taggedAs UiTag in new WebBrowser {
      go to UprnNotFoundPage

      click on setupTradeDetails

      page.title should equal(SetupTradeDetailsPage.title)
    }
  }

  "manualAddress button" should {
//    ToDo uncomment tests once EnterAddressManually has been implemented
//    "go to manualaddress page after the Manual Address button is clicked and trade details have been set up in cache" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//      CookieFactoryForUISpecs.setupTradeDetails()
//      go to UprnNotFoundPage
//
//      click on manualAddress
//
//      page.title should equal (EnterAddressManuallyPage.title)
//    }

//    "go to setuptradedetails page when trade details have not been set up in cache" taggedAs UiTag in new WebBrowser {
//      go to UprnNotFoundPage
//
//      click on manualAddress
//
//      page.title should equal(SetupTradeDetailsPage.title)
//    }
  }
}