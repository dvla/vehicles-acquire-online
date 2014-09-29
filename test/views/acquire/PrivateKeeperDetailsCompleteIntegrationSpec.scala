package views.acquire

import helpers.common.ProgressBar
import helpers.acquire.CookieFactoryForUISpecs
import ProgressBar.progressStep
import helpers.tags.UiTag
import helpers.UiSpec
import helpers.webbrowser.TestHarness
import org.openqa.selenium.{By, WebElement, WebDriver}
import pages.common.ErrorPanel
import pages.acquire.{BeforeYouStartPage,PrivateKeeperDetailsCompletePage,SetupTradeDetailsPage,PrivateKeeperDetailsPage}
import uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction
import webserviceclients.fakes.FakeAddressLookupService.addressWithUprn
import pages.acquire.PrivateKeeperDetailsCompletePage.{navigate, back, useTodaysDate, dayDateOfSaleTextBox, monthDateOfSaleTextBox, yearDateOfSaleTextBox}
import models.PrivateKeeperDetailsCompleteFormModel.Form.TodaysDateId
import webserviceclients.fakes.FakeDateServiceImpl.{DateOfAcquisitionDayValid, DateOfAcquisitionMonthValid, DateOfAcquisitionYearValid}

final class PrivateKeeperDetailsCompleteIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to PrivateKeeperDetailsCompletePage
      page.title should equal(PrivateKeeperDetailsCompletePage.title)
    }

    "display the progress of the page when progressBar is set to true" taggedAs UiTag in new ProgressBarTrue {
      go to BeforeYouStartPage
      cacheSetup()
      go to PrivateKeeperDetailsCompletePage
      page.source.contains(progressStep(7)) should equal(true)
    }

    "not display the progress of the page when progressBar is set to false" taggedAs UiTag in new ProgressBarFalse {
      go to BeforeYouStartPage
      cacheSetup()
      go to PrivateKeeperDetailsCompletePage
      page.source.contains(progressStep(7)) should equal(false)
    }

    "Redirect when no vehicle details are cached" taggedAs UiTag in new WebBrowser {
      go to PrivateKeeperDetailsCompletePage
      page.title should equal(SetupTradeDetailsPage.title)
    }

    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
      go to PrivateKeeperDetailsCompletePage
      val csrf: WebElement = webDriver.findElement(By.name(CsrfPreventionAction.TokenName))
      csrf.getAttribute("type") should equal("hidden")
      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
      csrf.getAttribute("value").size > 0 should equal(true)
    }
  }

  "submit button" should {
    "go to the appropriate next page when all private keeper complete details are entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate()
      page.title should equal("Not implemented") //ToDo change title when next page is implemented
    }

    "go to the appropriate next page when mandatory private keeper complete details are entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(
        dayDateOfBirth = "",
        monthDateOfBirth = "",
        yearDateOfBirth = "",
        mileage = ""
      )
      page.title should equal("Not implemented") //ToDo change title when next page is implemented
    }

    "display one validation error message when a mileage is entered greater than max length" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(mileage = "1000000")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a mileage is entered less than min length" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(mileage = "-1")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when a mileage containing letters is entered" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(mileage = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day date of birth contains letters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(dayDateOfBirth = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day month of birth contains letters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(monthDateOfBirth = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when year month of birth contains letters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(yearDateOfBirth = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day date of sale is empty" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(dayDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when month date of sale is empty" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(monthDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when year date of sale is empty" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(yearDateOfSale = "")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when day date of sale contains letters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(dayDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when month date of sale contains letters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(monthDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }

    "display one validation error message when year date of sale contains letters" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      navigate(yearDateOfSale = "a")
      ErrorPanel.numberOfErrors should equal(1)
    }
  }

  "use todays date" should {
    "input todays date into date of sale" taggedAs UiTag in new HtmlUnitWithJs {
      go to BeforeYouStartPage
      cacheSetup()
      go to PrivateKeeperDetailsCompletePage

      click on useTodaysDate

      dayDateOfSaleTextBox.value should equal (DateOfAcquisitionDayValid)
      monthDateOfSaleTextBox.value should equal (DateOfAcquisitionMonthValid)
      yearDateOfSaleTextBox.value should equal (DateOfAcquisitionYearValid)
    }

    "not display the Use Todays Date checkbox" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      cacheSetup()
      go to PrivateKeeperDetailsCompletePage

      webDriver.getPageSource shouldNot contain(TodaysDateId)
    }
  }

  "back" should {
    "display previous page when back link is clicked" taggedAs UiTag in new WebBrowser {
      go to BeforeYouStartPage
      CookieFactoryForUISpecs.
        setupTradeDetails().
        dealerDetails(addressWithUprn).
        vehicleDetails().
        privateKeeperDetails()

      go to PrivateKeeperDetailsCompletePage
      click on back
      page.title should equal(PrivateKeeperDetailsPage.title)
    }
  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      setupTradeDetails()
      .dealerDetails()
      .vehicleDetails()
      .privateKeeperDetails()
}
