package views.acquire

import helpers.common.ProgressBar
import helpers.acquire.CookieFactoryForUISpecs
import ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.acquire._
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import pages.common.Feedback.AcquireEmailFeedbackLink

final class AcquireSuccessIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireSuccessPage
      page.title should equal(AcquireSuccessPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireSuccessPage

      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }


    "redirect when the cache is missing acquire complete details" taggedAs UiTag in new WebBrowser {
      go to AcquireSuccessPage
      page.title should equal(BeforeYouStartPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireSuccessPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "Pressing buy another vehicle" should {
    "Should go to VehicleLookupPage on buy another" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireSuccessPage

      click on AcquireSuccessPage.buyAnother

      page.title should equal(VehicleLookupPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .dealerDetails()
      .acquireCompletionViewModel()
}