package views.acquire

import composition.TestHarness
import helpers.UiSpec
import helpers.common.ProgressBar
import helpers.tags.UiTag
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.HelpPage.{back, exit}
import pages.common.HelpPanel
import models.HelpCacheKey
import helpers.acquire.CookieFactoryForUISpecs
import pages.acquire.{HelpPage, VehicleLookupPage, BeforeYouStartPage}
import pages.common.Feedback.AcquireEmailFeedbackLink

final class HelpIntegrationSpec extends UiSpec with TestHarness {
  "go to page" ignore {
    "display the page containing correct title" taggedAs UiTag in new WebBrowserForSelenium {
      go to HelpPage
      pageTitle should equal(HelpPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to HelpPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "not display any progress indicator when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to HelpPage
      pageSource should not contain ProgressBar.div
    }
  }

  "back button" ignore {
    "redirect to the users previous page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage
      click on HelpPanel.help
      click on back
      pageTitle should equal(VehicleLookupPage.title)
    }

    "remove cookie" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      click on HelpPanel.help
      click on back
      webDriver.manage().getCookieNamed(HelpCacheKey) should equal(null)
    }
  }

  "exit" ignore {
    "redirect to the start page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage
      click on HelpPanel.help
      click on exit
      pageTitle should equal(BeforeYouStartPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails().
      dealerDetails()
}