package pages.acquire

import models.CompleteAndConfirmFormModel.Form.{ConsentId, DateOfSaleId, MileageId, TodaysDateId}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.Checkbox
import org.scalatest.selenium.WebBrowser.checkbox
import org.scalatest.selenium.WebBrowser.TelField
import org.scalatest.selenium.WebBrowser.telField
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.acquire.CompleteAndConfirm.{BackId, SubmitId}

object CompleteAndConfirmPage extends Page {
  final val address = buildAppUrl("complete-and-confirm")
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Complete and confirm"

  final val MileageValid = "1000"

  final val ValidDateOfSale = org.joda.time.DateTime.now.minusMonths(9)
  final val DayDateOfSaleValid = ValidDateOfSale.toString("dd")
  final val MonthDateOfSaleValid =  ValidDateOfSale.toString("MM")
  final val YearDateOfSaleValid = ValidDateOfSale.getYear.toString

  final val ConsentTrue = "Consent"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def useTodaysDate(implicit driver: WebDriver): Element = find(id(TodaysDateId)).get

  def mileageTextBox(implicit driver: WebDriver): TelField = telField(id(MileageId))

  def dayDateOfSaleTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfSaleId" + "_day"))

  def monthDateOfSaleTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfSaleId" + "_month"))

  def yearDateOfSaleTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfSaleId" + "_year"))

  def consent(implicit driver: WebDriver): Checkbox = checkbox(id(ConsentId))

  def navigate(mileage: String = MileageValid,
               dayDateOfSale: String = DayDateOfSaleValid,
               monthDateOfSale: String = MonthDateOfSaleValid,
               yearDateOfSale: String = YearDateOfSaleValid,
               consent: String = ConsentTrue)(implicit driver: WebDriver) = {
    go to CompleteAndConfirmPage

    mileageTextBox.value = mileage
    dayDateOfSaleTextBox.value = dayDateOfSale
    monthDateOfSaleTextBox.value = monthDateOfSale
    yearDateOfSaleTextBox.value = yearDateOfSale
    click on consent

    click on next
  }
}
