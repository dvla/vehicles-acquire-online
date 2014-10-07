package views.acquire

import helpers.common.ProgressBar
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import helpers.webbrowser.TestHarness
import ProgressBar.progressStep
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.common.ErrorPanel
import pages.acquire.NewKeeperChooseYourAddressPage.{back, sadPath, happyPath}
import webserviceclients.fakes.FakeAddressLookupService
import webserviceclients.fakes.FakeAddressLookupService.PostcodeValid
import pages.acquire.{PrivateKeeperDetailsPage, SetupTradeDetailsPage, NewKeeperChooseYourAddressPage, BeforeYouStartPage}
import pages.acquire.{BusinessKeeperDetailsPage, VehicleLookupPage, PrivateKeeperDetailsCompletePage, BusinessKeeperDetailsCompletePage}

final class NewKeeperChooseYourAddressIntegrationSpec extends UiSpec with TestHarness {
  "new keeper choose your address page" should {
    "display the page for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to NewKeeperChooseYourAddressPage
      page.title should equal(NewKeeperChooseYourAddressPage.title)
    }

    "display the page for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      go to NewKeeperChooseYourAddressPage
      page.title should equal(NewKeeperChooseYourAddressPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to NewKeeperChooseYourAddressPage

      page.source.contains(progressStep(6)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to NewKeeperChooseYourAddressPage

      page.source.contains(progressStep(6)) should equal(false)
    }

    "redirect when no traderBusinessName is cached" taggedAs UiTag in new WebBrowser {
      go to NewKeeperChooseYourAddressPage

      page.title should equal(SetupTradeDetailsPage.title)
    }

    "display appropriate content when address service returns addresses for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupCommon()
      cacheSetupPrivateKeeper()
      go to NewKeeperChooseYourAddressPage

      page.source.contains("No addresses found for that postcode") should equal(false) // Does not contain message
      page.source should include( """<a id="enterAddressManuallyButton" href""")
    }

    "display appropriate content when address service returns addresses for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupCommon()
      cacheSetupBusinessKeeper()
      go to NewKeeperChooseYourAddressPage

      page.source.contains("No addresses found for that postcode") should equal(false) // Does not contain message
      page.source should include( """<a id="enterAddressManuallyButton" href""")
    }

    "display the postcode entered in the previous page for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupCommon()
      cacheSetupPrivateKeeper()
      go to NewKeeperChooseYourAddressPage

      page.source.contains(FakeAddressLookupService.PostcodeValid.toUpperCase) should equal(true)
    }

    "display the postcode entered in the previous page for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupCommon()
      cacheSetupBusinessKeeper()
      go to NewKeeperChooseYourAddressPage

      page.source.contains(FakeAddressLookupService.PostcodeValid.toUpperCase) should equal(true)
    }

    "display expected addresses in dropdown when address service returns addresses for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupCommon()
      cacheSetupPrivateKeeper()
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
      cacheSetupCommon()
      cacheSetupBusinessKeeper()
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
      cacheSetupCommon()
      go to PrivateKeeperDetailsPage
      PrivateKeeperDetailsPage.submitPostcodeWithoutAddresses

      page.source should include("No addresses found for that postcode") // Does not contain the positive message
    }

    "display appropriate content when address service returns no addresses for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupCommon()
      go to BusinessKeeperDetailsPage
      BusinessKeeperDetailsPage.submitPostcodeWithoutAddresses

      page.source should include("No addresses found for that postcode") // Does not contain the positive message
    }

//    "manualAddress button that is displayed when addresses have been found" should { //ToDo implement test when enter address manually for new keeper is added
//      "go to the manual address entry page" taggedAs UiTag in new WebBrowser {
//        go to BeforeYouStartPage
//        cacheSetup()
//        go to BusinessChooseYourAddressPage
//
//        click on manualAddress
//
//        page.title should equal(EnterAddressManuallyPage.title)
//      }
//    }

//    "manualAddress button that is displayed when no addresses have been found" should { //ToDo implement test when enter address manually for new keeper is added
//      "go to the manual address entry page" taggedAs UiTag in new WebBrowser {
//        SetupTradeDetailsPage.submitPostcodeWithoutAddresses
//
//        click on manualAddress
//
//        page.title should equal(EnterAddressManuallyPage.title)
//      }
//    }

    "contain the hidden csrfToken field for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupCommon()
      cacheSetupPrivateKeeper()
      go to NewKeeperChooseYourAddressPage

      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }

    "contain the hidden csrfToken field for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupCommon()
      cacheSetupPrivateKeeper()
      go to NewKeeperChooseYourAddressPage

      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

    "back button" should {
      "display vehicle lookup" taggedAs UiTag in new WebBrowser {
        go to BeforeYouStartPage
        cacheSetupCommon()
        cacheSetupPrivateKeeper()
        go to NewKeeperChooseYourAddressPage

        click on back

        page.title should equal(VehicleLookupPage.title)
      }
    }


    "select button" should {
      "go to the next page when correct data is entered for a new private keeper" taggedAs UiTag in new WebBrowser {
        go to BeforeYouStartPage
        cacheSetupCommon()
        cacheSetupPrivateKeeper()

        happyPath

        page.title should equal(PrivateKeeperDetailsCompletePage.title)
      }

//      "go to the next page when correct data is entered for a new business keeper" taggedAs UiTag in new WebBrowser { //Todo Reimplement
//        go to BeforeYouStartPage
//        cacheSetupCommon()
//        cacheSetupBusinessKeeper()
//
//        happyPath
//
//        page.title should equal(BusinessKeeperDetailsCompletePage.title)
//      }

      "display validation error messages when addressSelected is not in the list for a new private keeper" taggedAs UiTag in new WebBrowser {
        go to BeforeYouStartPage
        cacheSetupCommon()
        cacheSetupPrivateKeeper()

        sadPath

        ErrorPanel.numberOfErrors should equal(1)
      }

      "display validation error messages when addressSelected is not in the list for a new business keeper" taggedAs UiTag in new WebBrowser {
        go to BeforeYouStartPage
        cacheSetupCommon()
        cacheSetupBusinessKeeper()

        sadPath

        ErrorPanel.numberOfErrors should equal(1)
      }
    }

  private def cacheSetupPrivateKeeper()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.privateKeeperDetails()

  private def cacheSetupBusinessKeeper()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.businessKeeperDetails()

  private def cacheSetupCommon()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()
      .vehicleDetails()
}