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

final class AcquireSuccessIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireSuccessPage
      page.title should equal(AcquireSuccessPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()
      go to CompleteAndConfirmPage
      page.source.contains(progressStep(7)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()
      go to CompleteAndConfirmPage
      page.source.contains(progressStep(7)) should equal(false)
    }

    "redirect when the cache is empty" taggedAs UiTag in new WebBrowser {
      go to CompleteAndConfirmPage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when the cache is missing dealer details" taggedAs UiTag in new WebBrowser {
      go to CompleteAndConfirmPage
      CookieFactoryForUISpecs
        .vehicleDetails()
        .newKeeperDetails()
        .completeAndConfirmDetails()

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when the cache is missing vehicle details" taggedAs UiTag in new WebBrowser {
      go to CompleteAndConfirmPage
      CookieFactoryForUISpecs
        .dealerDetails()
        .newKeeperDetails()
        .completeAndConfirmDetails()

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when the cache is missing new keeper details" taggedAs UiTag in new WebBrowser {
      go to CompleteAndConfirmPage
      CookieFactoryForUISpecs
        .dealerDetails()
        .vehicleDetails()
        .completeAndConfirmDetails()

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when the cache is missing complete and confirm  details" taggedAs UiTag in new WebBrowser {
      go to CompleteAndConfirmPage
      CookieFactoryForUISpecs
        .dealerDetails()
        .vehicleDetails()
        .newKeeperDetails()

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to CompleteAndConfirmPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .dealerDetails()
      .vehicleDetails()
      .newKeeperDetails()
      .completeAndConfirmDetails()
}