package views.acquire

import com.google.inject.Injector
import com.tzavellas.sse.guice.ScalaModule
import composition.{GlobalWithFilters, TestComposition, TestGlobalWithFilters, TestHarness}
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import models.CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey
import models.VehicleNewKeeperCompletionCacheKeys
import org.openqa.selenium.{By, JavascriptExecutor, WebElement, WebDriver}
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.mock.MockitoSugar
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.AcquireSuccessPage
import pages.acquire.BeforeYouStartPage
import pages.acquire.CompleteAndConfirmPage
import pages.acquire.CompleteAndConfirmPage.navigate
import pages.acquire.CompleteAndConfirmPage.back
import pages.acquire.CompleteAndConfirmPage.useTodaysDate
import pages.acquire.CompleteAndConfirmPage.mileageTextBox
import pages.acquire.CompleteAndConfirmPage.consent
import pages.acquire.CompleteAndConfirmPage.dayDateOfSaleTextBox
import pages.acquire.CompleteAndConfirmPage.monthDateOfSaleTextBox
import pages.acquire.CompleteAndConfirmPage.yearDateOfSaleTextBox
import pages.acquire.CompleteAndConfirmPage.next
import pages.acquire.MicroServiceErrorPage
import pages.acquire.SetupTradeDetailsPage
import pages.acquire.VehicleLookupPage
import pages.acquire.VehicleTaxOrSornPage
import pages.common.ErrorPanel
import pages.common.Feedback.AcquireEmailFeedbackLink
import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.TrackingId
import common.filters.CsrfPreventionAction
import common.helpers.webbrowser.WebDriverFactory
import common.mappings.TitleType
import common.testhelpers.LightFakeApplication
import common.webserviceclients.fakes.FakeAcquireWebServiceImpl
import common.webserviceclients.fakes.FakeAcquireWebServiceImpl.SimulateForbidden
import common.webserviceclients.fakes.FakeAcquireWebServiceImpl.SimulateMicroServiceUnavailable
import common.webserviceclients.fakes.FakeAcquireWebServiceImpl.SimulateSoapEndpointFailure
import common.webserviceclients.acquire.{AcquireRequestDto, AcquireWebService}
import webserviceclients.fakes.FakeAddressLookupService.addressWithoutUprn
import webserviceclients.fakes.FakeDateServiceImpl.TodayDay
import webserviceclients.fakes.FakeDateServiceImpl.TodayMonth
import webserviceclients.fakes.FakeDateServiceImpl.TodayYear

class CompleteAndConfirmIntegrationSpec extends UiSpec with TestHarness with Eventually with IntegrationPatience {

  "go to page" should {
    "display the page for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to CompleteAndConfirmPage

      pageTitle should equal(CompleteAndConfirmPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      go to CompleteAndConfirmPage

      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    "Redirect when no new keeper details are cached" taggedAs UiTag in new WebBrowserForSelenium {
      go to CompleteAndConfirmPage
      pageTitle should equal(SetupTradeDetailsPage.title)
      assertCookiesDoNotExist(Set(AllowGoingToCompleteAndConfirmPageCacheKey))
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowserForSelenium {
      go to CompleteAndConfirmPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").length > 0 should equal(true)
    }

    "redirect to vehicles lookup page if there is no cookie preventGoingToCompleteAndConfirmPage set" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .setupTradeDetails()
        .dealerDetails()
        .vehicleAndKeeperDetails()
        .newKeeperDetails()
        .vehicleLookupFormModel()
        .vehicleTaxOrSornFormModel()
      go to CompleteAndConfirmPage
      pageTitle should equal(VehicleLookupPage.title)
      assertCookiesDoNotExist(cookiesDeletedOnRedirect)
    }

    "redirect to Business choose your address page if there is no PreventGoingToCompleteAndConfirmPageCacheKey cookie set and there is no dealer details" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .setupTradeDetails()
        .vehicleAndKeeperDetails()
        .newKeeperDetails()
        .vehicleLookupFormModel()
        .vehicleTaxOrSornFormModel()
      go to CompleteAndConfirmPage
      pageTitle should equal(SetupTradeDetailsPage.title)
      assertCookiesDoNotExist(cookiesDeletedOnRedirect)
    }
  }

  "submit button" should {
    "go to the appropriate next page when all details are entered for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate()
      pageTitle should equal(AcquireSuccessPage.title)
    }

    "go to the success page when acquire fulfil returns 403 FORBIDDEN" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup(docReferenceNumber = SimulateForbidden)
      navigate()
      pageTitle should equal(AcquireSuccessPage.title)
    }

    "go to the microservice error page when acquire fulfil is unavailable" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup(docReferenceNumber = SimulateMicroServiceUnavailable)
      navigate()
      pageTitle should equal(MicroServiceErrorPage.title)
    }

    "go to the microservice error page when soap end point is down" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup(docReferenceNumber = SimulateSoapEndpointFailure)
      navigate()
      pageTitle should equal(MicroServiceErrorPage.title)
    }

    "go to the appropriate next page when mandatory details are entered for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(mileage = "")
      pageTitle should equal(AcquireSuccessPage.title)
    }

    "clear off the preventGoingToCompleteAndConfirmPage cookie on success" taggedAs UiTag in new PhantomJsByDefault {
      go to BeforeYouStartPage
      cacheSetup()
      assertCookieExist
      navigate()
      pageTitle should equal(AcquireSuccessPage.title)
      assertCookiesDoNotExist(Set(AllowGoingToCompleteAndConfirmPageCacheKey))
    }

    "clear off the preventGoingToCompleteAndConfirmPage cookie on error" taggedAs UiTag in new PhantomJsByDefault {
      import models.AcquireCacheKeyPrefix.CookiePrefix
      go to BeforeYouStartPage
      CookieFactoryForUISpecs
        .setupTradeDetails()
        .vehicleAndKeeperDetails()
        .newKeeperDetails()
        .vehicleLookupFormModel()
        .vehicleTaxOrSornFormModel()
        .preventGoingToCompleteAndConfirmPageCookie()
      // Use an invalid model to generate an exception that gets caught by the play OnError handler.
      webDriver.manage().addCookie(new org.openqa.selenium.Cookie(
          uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.traderDetailsCacheKey, "XXXXX")
      )
      assertCookieExist
      navigate()
      pageTitle should equal(play.api.i18n.Messages("error.title"))
      assertCookiesDoNotExist(Set(AllowGoingToCompleteAndConfirmPageCacheKey))
    }

    val countingWebService = new FakeAcquireWebServiceImpl {
      var calls = List[(AcquireRequestDto, TrackingId)]()

      override def callAcquireService(request: AcquireRequestDto, trackingId: TrackingId): Future[WSResponse] = {
        calls ++= List(request -> trackingId)
        super.callAcquireService(request, trackingId)
      }
    }

    object MockAcquireServiceCompositionGlobal extends GlobalWithFilters with TestComposition {
      override lazy val injector: Injector = TestGlobalWithFilters.testInjector(new ScalaModule with MockitoSugar {
        override def configure() {
          bind[AcquireWebService].toInstance(countingWebService)
        }
      })
    }

    "be disabled after click" taggedAs UiTag in new WebBrowserForSelenium(
      app = LightFakeApplication(global = MockAcquireServiceCompositionGlobal),
      webDriver = WebDriverFactory.webDriver("phantomjs", true)
    ) {
      go to BeforeYouStartPage
      cacheSetup()
      go to CompleteAndConfirmPage
      Eventually.eventually {
        pageTitle should equal(CompleteAndConfirmPage.title)
      }

      mileageTextBox.value = CompleteAndConfirmPage.MileageValid
      dayDateOfSaleTextBox.value = CompleteAndConfirmPage.DayDateOfSaleValid
      monthDateOfSaleTextBox.value = CompleteAndConfirmPage.MonthDateOfSaleValid
      yearDateOfSaleTextBox.value = CompleteAndConfirmPage.YearDateOfSaleValid
      click on consent

      val submitButton = next.underlying
      def clickSubmit(implicit driver: WebDriver) = driver.asInstanceOf[JavascriptExecutor]
        .executeScript("var clicks = 0; while (clicks < 5) {arguments[0].click(); clicks++;}", submitButton)

      submitButton.getAttribute("class") should not include "disabled"
      clickSubmit

      Eventually.eventually {
        countingWebService.calls should have size 1
      }
    }

    "display one validation error message when a mileage is entered greater than max length for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(mileage = "1000000")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a mileage is entered less than min length for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(mileage = "-1")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a mileage containing letters is entered for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(mileage = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day date of sale is empty for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(dayDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when month date of sale is empty for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(monthDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when year date of sale is empty for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(yearDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day date of sale contains letters for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(dayDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when month date of sale contains letters for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(monthDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when year date of sale contains letters for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(yearDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "redirect to vehicles lookup page if there is no PreventGoingToCompleteAndConfirmPageCacheKey cookie set" taggedAs UiTag in new PhantomJsByDefault {
      def deleteFlagCookie(implicit driver: WebDriver) =
        driver.manage.deleteCookieNamed(AllowGoingToCompleteAndConfirmPageCacheKey)

      go to BeforeYouStartPage
      cacheSetup()
      go to CompleteAndConfirmPage

      mileageTextBox.value = CompleteAndConfirmPage.MileageValid
      dayDateOfSaleTextBox.value = CompleteAndConfirmPage.DayDateOfSaleValid
      monthDateOfSaleTextBox.value = CompleteAndConfirmPage.MonthDateOfSaleValid
      yearDateOfSaleTextBox.value = CompleteAndConfirmPage.YearDateOfSaleValid
      click on consent

      deleteFlagCookie

      click on next
      pageTitle should equal(VehicleLookupPage.title)
    }
  }

  "use todays date" should {
    "input todays date into date of sale for a new keeper" taggedAs UiTag in new WebBrowserWithJs {
      go to BeforeYouStartPage
      cacheSetup()
      go to CompleteAndConfirmPage

      eventually {
        click on useTodaysDate

        dayDateOfSaleTextBox.value should equal (TodayDay)
        monthDateOfSaleTextBox.value should equal (TodayMonth)
        yearDateOfSaleTextBox.value should equal (TodayYear)
      }
    }
  }

  "back" should {
    "display previous page when back link is clicked for a new keeper" taggedAs UiTag in new WebBrowserForSelenium {
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
        .preventGoingToCompleteAndConfirmPageCookie()

      go to CompleteAndConfirmPage
      click on back
      pageTitle should equal(VehicleTaxOrSornPage.title)
    }
  }

  private def cacheSetup(docReferenceNumber: String)(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .setupTradeDetails()
      .dealerDetails()
      .vehicleAndKeeperDetails()
      .newKeeperDetails()
      .vehicleLookupFormModel(referenceNumber = docReferenceNumber)
      .vehicleTaxOrSornFormModel()
      .preventGoingToCompleteAndConfirmPageCookie()

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs
      .setupTradeDetails()
      .dealerDetails()
      .vehicleAndKeeperDetails()
      .newKeeperDetails()
      .vehicleLookupFormModel()
      .vehicleTaxOrSornFormModel()
      .preventGoingToCompleteAndConfirmPageCookie()

  private val cookiesDeletedOnRedirect =
    VehicleNewKeeperCompletionCacheKeys ++ Set(AllowGoingToCompleteAndConfirmPageCacheKey)

  private def assertCookiesDoNotExist(cookies: Set[String])(implicit driver: WebDriver) =
    for (cookie <- cookies) driver.manage().getCookieNamed(cookie) should be (null)

  private def assertCookieExist(implicit driver: WebDriver) =
    driver.manage().getCookieNamed(AllowGoingToCompleteAndConfirmPageCacheKey) should not be null
}
