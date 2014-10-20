package views.acquire

import helpers.common.ProgressBar
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.common.ErrorPanel
import pages.acquire.BeforeYouStartPage
import pages.acquire.BusinessKeeperDetailsPage
import pages.acquire.PrivateKeeperDetailsPage
import pages.acquire.NewKeeperChooseYourAddressPage
import pages.acquire.NewKeeperEnterAddressManuallyPage
import pages.acquire.VehicleLookupPage
import pages.acquire.VehicleTaxOrSornPage
import pages.acquire.NewKeeperChooseYourAddressPage.{back, manualAddress, sadPath, happyPath}
import ProgressBar.progressStep
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import webserviceclients.fakes.FakeAddressLookupService
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValid
import pages.common.Feedback.AcquireEmailFeedbackLink

final class NewKeeperChooseYourAddressIntegrationSpec extends UiSpec with TestHarness {
  "new keeper choose your address page" should {
    "display the page for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      go to NewKeeperChooseYourAddressPage
      page.title should equal(NewKeeperChooseYourAddressPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      go to NewKeeperChooseYourAddressPage
      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "display the page for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper
      go to NewKeeperChooseYourAddressPage
      page.title should equal(NewKeeperChooseYourAddressPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      go to NewKeeperChooseYourAddressPage
      page.source.contains(progressStep(6)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      go to NewKeeperChooseYourAddressPage
      page.source.contains(progressStep(6)) should equal(false)
    }

    "redirect to vehicle lookup when no keeper cookies are in cache" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .setupTradeDetails()
        .dealerDetails()
        .vehicleDetails()
      go to NewKeeperChooseYourAddressPage
      page.title should equal(VehicleLookupPage.title)
    }

    "redirect to vehicle lookup when cookies are in cache for both private and business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      cacheSetupBusinessKeeper
      go to NewKeeperChooseYourAddressPage
      page.title should equal(VehicleLookupPage.title)
    }

    "redirect to vehicle lookup when no vehicle cookies are in cache but private keeper details exist" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .setupTradeDetails()
        .dealerDetails()
        .privateKeeperDetails()
      go to NewKeeperChooseYourAddressPage
      page.title should equal(VehicleLookupPage.title)
    }

    "redirect to vehicle lookup when no vehicle cookies are in cache but business keeper details exist" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .setupTradeDetails()
        .dealerDetails()
        .businessKeeperDetails()
      go to NewKeeperChooseYourAddressPage
      page.title should equal(VehicleLookupPage.title)
    }

    "display appropriate content when address service returns addresses for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      go to NewKeeperChooseYourAddressPage
      page.source.contains("No addresses found for that postcode") should equal(false) // Does not contain message
      page.source should include( """<a id="enterAddressManuallyButton" href""")
    }

    "display appropriate content when address service returns addresses for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper
      go to NewKeeperChooseYourAddressPage
      page.source.contains("No addresses found for that postcode") should equal(false) // Does not contain message
      page.source should include( """<a id="enterAddressManuallyButton" href""")
    }

    "display the postcode entered in the previous page for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      go to NewKeeperChooseYourAddressPage
      page.source.contains(FakeAddressLookupService.PostcodeValid.toUpperCase) should equal(true)
    }

    "display the postcode entered in the previous page for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper
      go to NewKeeperChooseYourAddressPage
      page.source.contains(FakeAddressLookupService.PostcodeValid.toUpperCase) should equal(true)
    }

    "display expected addresses in dropdown when address service returns addresses for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      go to NewKeeperChooseYourAddressPage

      NewKeeperChooseYourAddressPage.getListCount should equal(4) // The first option is the "Please select..." and the other options are the addresses.
      page.source should include(
        s"presentationProperty stub, 123, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      page.source should include(
        s"presentationProperty stub, 456, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      page.source should include(
        s"presentationProperty stub, 789, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
    }

    "display expected addresses in dropdown when address service returns addresses for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper
      go to NewKeeperChooseYourAddressPage

      NewKeeperChooseYourAddressPage.getListCount should equal(4) // The first option is the "Please select..." and the other options are the addresses.
      page.source should include(
        s"presentationProperty stub, 123, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      page.source should include(
        s"presentationProperty stub, 456, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
      page.source should include(
        s"presentationProperty stub, 789, property stub, street stub, town stub, area stub, $PostcodeValid"
      )
    }

    "display appropriate content when address service returns no addresses for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .setupTradeDetails()
        .dealerDetails()
        .vehicleDetails()
      go to PrivateKeeperDetailsPage
      PrivateKeeperDetailsPage.submitPostcodeWithoutAddresses
      page.source should include("No addresses found for that postcode") // Does not contain the positive message
    }

    "display appropriate content when address service returns no addresses for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .setupTradeDetails()
        .dealerDetails()
        .vehicleDetails()
      go to BusinessKeeperDetailsPage
      BusinessKeeperDetailsPage.submitPostcodeWithoutAddresses
      page.source should include("No addresses found for that postcode") // Does not contain the positive message
    }

    "allow navigation to manual address entry when addresses have been found" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      go to NewKeeperChooseYourAddressPage
      click on manualAddress
      page.title should equal(NewKeeperEnterAddressManuallyPage.title)
    }

    "allow navigation to manual address entry when no addresses have been found" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      PrivateKeeperDetailsPage.submitPostcodeWithoutAddresses
      click on manualAddress
      page.title should equal(NewKeeperEnterAddressManuallyPage.title)
    }

    "contain the hidden csrfToken field for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to NewKeeperChooseYourAddressPage

      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }

    "contain the hidden csrfToken field for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to NewKeeperChooseYourAddressPage

      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "back button" should {
    "display private keeper details page when private keeper cookie is in cache" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      go to NewKeeperChooseYourAddressPage
      click on back
      page.title should equal(PrivateKeeperDetailsPage.title)
    }

    "display business keeper details page when business keeper cookie is in cache" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper
      go to NewKeeperChooseYourAddressPage
      click on back
      page.title should equal(BusinessKeeperDetailsPage.title)
    }
  }

  "select button" should {
    "go to the next page when correct data is entered for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      happyPath
      page.title should equal(VehicleTaxOrSornPage.title)
    }

    "go to the next page when correct data is entered for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper
      happyPath
      page.title should equal(VehicleTaxOrSornPage.title)
      }

    "display validation error messages when addressSelected is not in the list for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper
      sadPath
      ErrorPanel.numberOfErrors should equal(1)
    }


    "display validation error messages when addressSelected is not in the list for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper
      sadPath
      ErrorPanel.numberOfErrors should equal(1)
    }
  }

  private def cacheSetupPrivateKeeper()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()
      .vehicleDetails()
      .privateKeeperDetails()

  private def cacheSetupBusinessKeeper()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()
      .vehicleDetails()
      .businessKeeperDetails()
}
