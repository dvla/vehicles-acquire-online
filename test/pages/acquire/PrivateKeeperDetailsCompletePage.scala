package pages.acquire

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory, Element, SingleSel, EmailField, TextField}
import org.openqa.selenium.WebDriver
import views.acquire.PrivateKeeperDetailsComplete.{BackId, SubmitId}
import models.PrivateKeeperDetailsCompleteFormModel.Form.MileageId

object PrivateKeeperDetailsCompletePage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/private-complete-and-confirm"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Complete and confirm"

  final val DayDateOfBirthValid = "24"
  final val MonthDateOfBirthValid = "12"
  final val YearDateOfBirthValid = "1920"
  final val MileageValid = "1000"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  //def dayDateOfBirthTextBox(implicit driver: WebDriver): EmailField = emailField(id(EmailId))

  def mileageTextBox(implicit driver: WebDriver): EmailField = emailField(id(MileageId))
}
