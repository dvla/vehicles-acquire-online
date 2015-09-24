package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import helpers.common.ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.UiSpec
import org.openqa.selenium.{By, WebElement, WebDriver}
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.BeforeYouStartPage
import pages.acquire.NewKeeperChooseYourAddressPage
import pages.acquire.NewKeeperEnterAddressManuallyPage
import pages.acquire.VehicleTaxOrSornPage
import pages.acquire.VehicleTaxOrSornPage.back
import pages.common.Feedback.AcquireEmailFeedbackLink
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.mappings.TitleType
import webserviceclients.fakes.FakeAddressLookupService.{addressWithUprn, addressWithoutUprn}

class VehicleTaxOrSornIntegrationSpec extends UiSpec with TestHarness{

  "go to page" should {
    "display the page for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage
      pageTitle should equal(VehicleTaxOrSornPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage
      pageSource.contains(progressStep(7)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage
      pageSource.contains(progressStep(7)) should equal(false)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to VehicleTaxOrSornPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").nonEmpty should equal(true)
    }

    "load EVL web site when tax selected" taggedAs UiTag in new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage

      click on VehicleTaxOrSornPage.taxSelect
      click on VehicleTaxOrSornPage.next

      webDriver.getWindowHandles.size should equal(2)

      // TODO: Commented for now out because when it runs on jenkins we cannot access the url
      // (it may be a skyscape routing problem) catch up with chris about this once he gets an update
//      import scala.collection.JavaConversions.asScalaSet
//      webDriver.getWindowHandles.filterNot(_ == webDriver.getWindowHandle).foreach(winHandle => {
//          webDriver.switchTo().window(winHandle)
//          webDriver.getCurrentUrl should equal("https://www.gov.uk/vehicle-tax")
//        }
//      )
    }
  }

  "back" should {
    "display new keeper choose your address page when back is clicked after the address has been selected from the drop down" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn).
        vehicleAndKeeperDetails().
        privateKeeperDetails().
        newKeeperDetails(
          title = Some(TitleType(1,"")),
          address = addressWithUprn
        ).vehicleTaxOrSornFormModel()

      go to VehicleTaxOrSornPage
      click on back
      pageTitle should equal(NewKeeperChooseYourAddressPage.title)
    }

    "display new keeper enter address manually page when back is clicked after the new keeper has entered the address manually" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails().
        vehicleAndKeeperDetails().
        privateKeeperDetails().
        newKeeperEnterAddressManually().
        newKeeperDetails(
          title = Some(TitleType(1,"")),
          address = addressWithoutUprn
        ).vehicleTaxOrSornFormModel()

      go to VehicleTaxOrSornPage
      click on back
      pageTitle should equal(NewKeeperEnterAddressManuallyPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()
      .vehicleAndKeeperDetails()
      .newKeeperDetails()
      .vehicleLookupFormModel()
}
