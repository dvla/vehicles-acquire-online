package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import helpers.common.ProgressBar
import helpers.tags.UiTag
import helpers.UiSpec
import models.AcquireCacheKeyPrefix.CookiePrefix
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.common.ErrorPanel
import pages.acquire.BeforeYouStartPage
import pages.acquire.BusinessChooseYourAddressPage
import pages.acquire.BusinessKeeperDetailsPage
import pages.acquire.SetupTradeDetailsPage
import pages.acquire.VehicleLookupPage
import pages.acquire.VehicleLookupPage.{happyPath, back}
import pages.acquire.PrivateKeeperDetailsPage
import pages.acquire.KeeperStillOnRecordPage
import pages.common.Feedback.AcquireEmailFeedbackLink
import ProgressBar.progressStep
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import uk.gov.dvla.vehicles.presentation.common.model.BusinessKeeperDetailsFormModel.businessKeeperDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.PrivateKeeperDetailsFormModel.privateKeeperDetailsCacheKey
import webserviceclients.fakes.FakeAddressLookupService.addressWithUprn
import uk.gov.dvla.vehicles.presentation.common.testhelpers.LightFakeApplication

class VehicleLookupIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage
      page.title should equal(VehicleLookupPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage
      page.source.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage
      page.source.contains(progressStep(4)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()
      go to VehicleLookupPage
      page.source.contains(progressStep(4)) should equal(false)
    }

    "Redirect when no traderBusinessName is cached" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "redirect when no dealerBusinessName is cached" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to VehicleLookupPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").length > 0 should equal(true)
    }
  }

  "next button" should {
    "go to the appropriate next page when vehicle keeper is still on record" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      val vehicleWithKeeperStillOnRecordRefNumber = "99999999993"
      happyPath(referenceNumber = vehicleWithKeeperStillOnRecordRefNumber)
      page.title should equal(KeeperStillOnRecordPage.title)
    }

    "go to the appropriate next page when private keeper data is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath()
      page.title should equal(PrivateKeeperDetailsPage.title)
    }

    "clear businessKeeperDetails when private keeper data is entered" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup()
      CookieFactoryForUISpecs.businessKeeperDetails()
      happyPath()
      webDriver.manage().getCookieNamed(businessKeeperDetailsCacheKey) should equal(null)
    }

    "go to the appropriate next page when business keeper data is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath(isVehicleSoldToPrivateIndividual = false)
      page.title should equal(BusinessKeeperDetailsPage.title)
    }

    "clear privateKeeperDetails when business keeper data is entered" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup()
      CookieFactoryForUISpecs.privateKeeperDetails()
      happyPath(isVehicleSoldToPrivateIndividual = false)
      webDriver.manage().getCookieNamed(privateKeeperDetailsCacheKey) should equal(null)
    }

    "display one validation error message when no referenceNumber is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath(referenceNumber = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when no registrationNumber is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath(registrationNumber = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a registrationNumber is entered containing one character" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath(registrationNumber = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a registrationNumber is entered containing special characters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath(registrationNumber = "$^")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display two validation error messages when no vehicle details are entered but consent is given" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath(referenceNumber = "", registrationNumber = "")
      ErrorPanel.numberOfErrors should equal(2)
    }

    "display one validation error message when only a valid registrationNumber is entered and consent is given" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath(registrationNumber = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when invalid referenceNumber (Html5Validation disabled)" taggedAs UiTag in new WebBrowser(app = fakeAppWithHtml5ValidationDisabledConfig) {
      go to BeforeYouStartPage
      cacheSetup()
      happyPath(referenceNumber = "")
      ErrorPanel.numberOfErrors should equal(1)
    }
  }

  "back" should {
    "display previous page when back link is clicked with uprn present" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn)

      go to VehicleLookupPage
      click on back
      page.title should equal(BusinessChooseYourAddressPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()

  private val fakeAppWithHtml5ValidationEnabledConfig = LightFakeApplication(global, Map("html5Validation.enabled" -> true))

  private val fakeAppWithHtml5ValidationDisabledConfig = LightFakeApplication(global, Map("html5Validation.enabled" -> false))
}
