package views.acquire

import helpers.UiSpec
import helpers.common.ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement}
//import pages.common.Accessibility
//import pages.common.ErrorPanel
//import pages.common.{Accessibility, ErrorPanel}
import pages.acquire.SetupTradeDetailsPage._
import pages.acquire.SetupTradeDetailsPage
import pages.acquire.SetupTradeDetailsPage
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import viewmodels.SetupTradeDetailsViewModel

final class SetupTradeDetailsIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to SetupTradeDetailsPage

      page.title should equal(SetupTradeDetailsPage.title)
    }
  }

  "lookup button" should {
    "go to the next page when correct data is entered" taggedAs UiTag in new WebBrowser {
      happyPath()

      page.title should equal("Success")
    }
  }


}