package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.acquire.BeforeYouStartPage
import pages.acquire.NewKeeperChooseYourAddressPage
import pages.acquire.NewKeeperEnterAddressManuallyPage
import pages.acquire.VehicleTaxOrSornPage
import pages.acquire.VehicleTaxOrSornPage.back
import pages.acquire.CompleteAndConfirmPage
import pages.acquire.AcquireSuccessPage
import pages.common.Feedback.AcquireEmailFeedbackLink
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.mappings.TitleType
import webserviceclients.fakes.FakeAddressLookupService.addressWithoutUprn
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go, pageTitle, pageSource}

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
    }
    "display sorn entered message on acquire success page when SORN selected" taggedAs UiTag in new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleTaxOrSornPage

      click on VehicleTaxOrSornPage.sornSelect
      click on VehicleTaxOrSornPage.next

      go to CompleteAndConfirmPage
      click on CompleteAndConfirmPage.consent
      click on CompleteAndConfirmPage.useTodaysDate
      click on CompleteAndConfirmPage.next

      go to AcquireSuccessPage
      pageSource should include ("DVLA will not send you a SORN acknowledgement letter. The SORN will be valid until the vehicle is taxed, sold, permanently exported or scrapped. Please ensure that the vehicle is taxed before it is driven on public roads.")
      pageSource should not include ("If the vehicle has not been taxed during this service it will need to be before being driven on the road.")
    }

  }

  "back" should {
    "display new keeper choose your address page when back is clicked after the address has been selected from the drop down" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithoutUprn).
        vehicleAndKeeperDetails().
        privateKeeperDetails().
        newKeeperDetails(
          title = Some(TitleType(1,"")),
          address = addressWithoutUprn
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
