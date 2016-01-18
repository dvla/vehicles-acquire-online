package views.acquire

import composition.TestHarness
import helpers.UiSpec
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import org.openqa.selenium.{By, WebElement, WebDriver}
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.acquire.BeforeYouStartPage
import pages.acquire.VrmLockedPage
import pages.acquire.VrmLockedPage.exit

class VrmLockedUiSpec extends UiSpec with TestHarness{

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage

      currentUrl should equal(VrmLockedPage.url)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to VrmLockedPage
      val csrf: WebElement = webDriver.findElement(
        By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      )
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should
        equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").length > 0 should equal(true)
    }

    "contain the time of locking" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage
      val localTime: WebElement = webDriver.findElement(By.id("localTimeOfVrmLock"))
      localTime.isDisplayed should equal(true)
      localTime.getText.contains("UTC") should equal (true)
    }

    "contain the time of locking when JavaScript is disabled" taggedAs UiTag in new WebBrowserWithJsDisabled {
      go to BeforeYouStartPage
      cacheSetup
      go to VrmLockedPage
      val localTime: WebElement = webDriver.findElement(By.id("localTimeOfVrmLock"))
      localTime.isDisplayed should equal(true)
      localTime.getText.contains("UTC") should equal (true)
    }
  }

  "exit button" should {
    "redirect to feedback page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VrmLockedPage
      click on exit
      currentUrl should equal(BeforeYouStartPage.url)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails().
      bruteForcePreventionViewModel()
}