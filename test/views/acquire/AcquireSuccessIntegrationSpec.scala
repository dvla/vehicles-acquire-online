package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import helpers.UiSpec
import org.openqa.selenium.{By, WebElement, WebDriver}
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.{AcquireSuccessPage, BeforeYouStartPage, VehicleLookupPage}
import pages.common.Feedback.AcquireEmailFeedbackLink
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.testhelpers.UiTag

class AcquireSuccessIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireSuccessPage
      pageTitle should equal(AcquireSuccessPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireSuccessPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "redirect when the cache is missing acquire complete details" taggedAs UiTag in new WebBrowserForSelenium {
      go to AcquireSuccessPage
      pageTitle should equal(BeforeYouStartPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to AcquireSuccessPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").nonEmpty should equal(true)
    }
  }

  "Clicking buy another vehicle button" should {
    "go to VehicleLookupPage" taggedAs UiTag in new WebBrowserForSelenium {
      buyAnotherVehicle()
    }

    "go to VehicleLookupPage with ceg identifier cookie" taggedAs UiTag in new WebBrowserForSelenium {
      buyAnotherVehicle(ceg = true)
    }
  }

  private def buyAnotherVehicle(ceg: Boolean = false)(implicit webDriver: WebDriver) = {
    val identifier = "CEG"
    go to BeforeYouStartPage
    if (ceg) cacheSetup().withIdentifier(identifier)
    else cacheSetup()
    go to AcquireSuccessPage
    click on AcquireSuccessPage.buyAnother
    pageTitle should equal(VehicleLookupPage.title)
    if (ceg)
      webDriver.manage.getCookieNamed(models.IdentifierCacheKey).getValue() should equal(identifier)
    else
      webDriver.manage.getCookieNamed(models.IdentifierCacheKey) should equal(null)
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
