package views.acquire

import helpers.common.ProgressBar
import helpers.acquire.CookieFactoryForUISpecs
import ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.common.ErrorPanel
import pages.acquire.{BeforeYouStartPage,CompleteAndConfirmPage,SetupTradeDetailsPage,NewKeeperChooseYourAddressPage}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import webserviceclients.fakes.FakeAddressLookupService.addressWithUprn
import pages.acquire.CompleteAndConfirmPage.{navigate, back, useTodaysDate, dayDateOfSaleTextBox, monthDateOfSaleTextBox, yearDateOfSaleTextBox}
import models.CompleteAndConfirmFormModel.Form.TodaysDateId
import webserviceclients.fakes.FakeDateServiceImpl.{DateOfAcquisitionDayValid, DateOfAcquisitionMonthValid, DateOfAcquisitionYearValid}

final class CompleteAndConfirmIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page for a new private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to CompleteAndConfirmPage
      page.title should equal(CompleteAndConfirmPage.title)
    }

    "display the page for a new business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      go to CompleteAndConfirmPage
      page.title should equal(CompleteAndConfirmPage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to CompleteAndConfirmPage
      page.source.contains(progressStep(7)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to CompleteAndConfirmPage
      page.source.contains(progressStep(7)) should equal(false)
    }

    "Redirect when no vehicle details are cached" taggedAs UiTag in new WebBrowser {
      go to CompleteAndConfirmPage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to CompleteAndConfirmPage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "submit button" should {
    "go to the appropriate next page when all details are entered for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate()
      page.title should equal("Not implemented") //ToDo change title when next page is implemented
    }

    "go to the appropriate next page when all details are entered for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate()
      page.title should equal("Not implemented") //ToDo change title when next page is implemented
    }

    "go to the appropriate next page when mandatory details are entered for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(mileage = "")
      page.title should equal("Not implemented") //ToDo change title when next page is implemented
    }

    "go to the appropriate next page when mandatory details are entered for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(mileage = "")
      page.title should equal("Not implemented") //ToDo change title when next page is implemented
    }

    "display one validation error message when a mileage is entered greater than max length for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(mileage = "1000000")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a mileage is entered greater than max length for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(mileage = "1000000")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a mileage is entered less than min length for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(mileage = "-1")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a mileage is entered less than min length for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(mileage = "-1")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a mileage containing letters is entered for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(mileage = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a mileage containing letters is entered for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(mileage = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day date of sale is empty for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(dayDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day date of sale is empty for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(dayDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when month date of sale is empty for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(monthDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when month date of sale is empty for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(monthDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when year date of sale is empty for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(yearDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when year date of sale is empty for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(yearDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day date of sale contains letters for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(dayDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day date of sale contains letters for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(dayDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when month date of sale contains letters for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(monthDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when month date of sale contains letters for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(monthDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when year date of sale contains letters for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      navigate(yearDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when year date of sale contains letters for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupBusinessKeeper()
      navigate(yearDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }
  }

  "use todays date" should {
    "input todays date into date of sale for a private keeper" taggedAs UiTag in new HtmlUnitWithJs {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to CompleteAndConfirmPage

      click on useTodaysDate

      dayDateOfSaleTextBox.value should equal (DateOfAcquisitionDayValid)
      monthDateOfSaleTextBox.value should equal (DateOfAcquisitionMonthValid)
      yearDateOfSaleTextBox.value should equal (DateOfAcquisitionYearValid)
    }

    "not display the Use Todays Date checkbox for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetupPrivateKeeper()
      go to CompleteAndConfirmPage

      webDriver.getPageSource shouldNot contain(TodaysDateId)
    }
  }

  "back" should {
    "display previous page when back link is clicked for a private keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn).
        vehicleDetails().
        privateKeeperDetails().
        newKeeperDetails(addressWithUprn)

      go to CompleteAndConfirmPage
      click on back
      page.title should equal(NewKeeperChooseYourAddressPage.title)
    }

    "display previous page when back link is clicked for a business keeper" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn).
        vehicleDetails().
        businessKeeperDetails().
        newKeeperDetails(addressWithUprn)

      go to CompleteAndConfirmPage
      click on back
      page.title should equal(NewKeeperChooseYourAddressPage.title)
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