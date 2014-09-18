package pages.acquire

import helpers.webbrowser._
import org.openqa.selenium.WebDriver
import views.acquire.PrivateKeeperDetailsComplete.{BackId, SubmitId}
import models.PrivateKeeperDetailsCompleteFormModel.Form.{MileageId, DateOfBirthId, ConsentId}
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear._

object PrivateKeeperDetailsCompletePage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/private-complete-and-confirm"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Complete and confirm"

  final val DayDateOfBirthValid = "24"
  final val MonthDateOfBirthValid = "12"
  final val YearDateOfBirthValid = "1920"
  final val MileageValid = "1000"
  final val ConsentTrue = "consent"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def dayDateOfBirthTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfBirthId" + "_day"))

  def monthDateOfBirthTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfBirthId" + "_month"))

  def yearDateOfBirthTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfBirthId" + "_year"))

  def mileageTextBox(implicit driver: WebDriver): TelField = telField(id(MileageId))

  def consent(implicit driver: WebDriver): Checkbox = checkbox(id(ConsentId))

  def navigate(dayDateOfBirth: String = DayDateOfBirthValid,
               monthDateOfBirth: String = MonthDateOfBirthValid,
               yearDateOfBirth: String = YearDateOfBirthValid,
               mileage: String = MileageValid,
               consent: String = ConsentTrue)(implicit driver: WebDriver) = {
    go to PrivateKeeperDetailsCompletePage

    dayDateOfBirthTextBox enter dayDateOfBirth
    monthDateOfBirthTextBox enter monthDateOfBirth
    yearDateOfBirthTextBox enter yearDateOfBirth
    mileageTextBox enter mileage
    click on consent

    click on next
  }
}
