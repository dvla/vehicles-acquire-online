package views.acquire

import helpers.UiSpec
import helpers.acquire.CookieFactoryForUISpecs
import helpers.common.ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.acquire.BeforeYouStartPage
import pages.acquire.NewKeeperChooseYourAddressPage
import pages.acquire.NewKeeperEnterAddressManuallyPage
import pages.acquire.VehicleTaxOrSornPage
import pages.acquire.VehicleTaxOrSornPage.back
import pages.common.Feedback.AcquireEmailFeedbackLink
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.mappings.TitleType
import webserviceclients.fakes.FakeAddressLookupService.{addressWithUprn, addressWithoutUprn}

final class VehicleTaxOrSornIntegrationSpec extends UiSpec with TestHarness{

  "go to page" should {
    "display the page for a new keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage
      page.title should equal(VehicleTaxOrSornPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage
      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage
      page.source.contains(progressStep(7)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage
      page.source.contains(progressStep(7)) should equal(false)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VehicleTaxOrSornPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "back" should {
    "display new keeper choose your address page when back is clicked after the address has been selected from the drop down" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn).
        vehicleDetails().
        privateKeeperDetails().
        newKeeperDetails(
          title = Some(TitleType(1,"")),
          address = addressWithUprn
        ).vehicleTaxOrSornFormModel()

      go to VehicleTaxOrSornPage
      click on back
      page.title should equal(NewKeeperChooseYourAddressPage.title)
    }

    "display new keeper enter address manually page when back is clicked after the new keeper has entered the address manually" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails().
        vehicleDetails().
        privateKeeperDetails().
        newKeeperDetails(
          title = Some(TitleType(1,"")),
          address = addressWithoutUprn
        ).vehicleTaxOrSornFormModel()

      go to VehicleTaxOrSornPage
      click on back
      page.title should equal(NewKeeperEnterAddressManuallyPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()
      .vehicleDetails()
      .newKeeperDetails()
      .vehicleLookupFormModel()

}