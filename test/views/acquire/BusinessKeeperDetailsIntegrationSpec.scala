package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import org.openqa.selenium.{By, WebElement, WebDriver}
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.common.ErrorPanel
import pages.acquire.{BeforeYouStartPage, BusinessKeeperDetailsPage, NewKeeperChooseYourAddressPage, VehicleLookupPage}
import pages.acquire.BusinessKeeperDetailsPage.{navigate, back}
import pages.common.Feedback.AcquireEmailFeedbackLink
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.testhelpers.{UiSpec, UiTag}
import webserviceclients.fakes.FakeAddressLookupService.addressWithoutUprn

class BusinessKeeperDetailsIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessKeeperDetailsPage
      pageTitle should equal(BusinessKeeperDetailsPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to BusinessKeeperDetailsPage

      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to BusinessKeeperDetailsPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "next button" should {
    "go to the appropriate next page when all new keeper details are entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate()
      pageTitle should equal (NewKeeperChooseYourAddressPage.title)
    }

    "display one validation error message when an incorrect business name is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(businessName = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when an incorrect postcode is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(postcode = "Q9")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when an incorrect email is entered" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(email = Some("aaa.com"))
      ErrorPanel.numberOfErrors should equal(1)
    }
  }

  "back" should {
    "display previous page when back button is clicked" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithoutUprn).
        vehicleAndKeeperDetails()

      go to BusinessKeeperDetailsPage
      click on back
      pageTitle should equal(VehicleLookupPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()
      .vehicleAndKeeperDetails()
}
