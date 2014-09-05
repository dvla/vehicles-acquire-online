package pages.acquire

import helpers.webbrowser._
import org.openqa.selenium.WebDriver
import views.acquire.PrivateKeeperDetails._
import viewmodels.PrivateKeeperDetailsViewModel.Form._
import views.acquire.PrivateKeeperDetails.BackId

object PrivateKeeperDetailsPage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/private-keeper-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter keeper details"

  final val TitleValid = "Mrs"
  final val TitleInvalid = ""
  final val OptionValid = "firstOption"
  final val EmailValid = "my@email.com"
  final val EmailInvalid = "no_at_symbol.com"
  final val TitleInvalidError = "Please select a title from the drop down list."
  final val VehicleMakeValid = "Audi"
  final val ModelValid = "A6"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def titleDropDown(implicit driver: WebDriver): SingleSel = singleSel(id(TitleId))

  def emailTextBox(implicit driver: WebDriver): TextField = textField(id(EmailId))

  def happyPath(title: String = OptionValid, email: String = EmailValid)(implicit driver: WebDriver) = {
    go to PrivateKeeperDetailsPage

    titleDropDown select title
    emailTextBox enter email

    click on next
  }

  def sadPath (title: String = TitleInvalid, email: String = EmailInvalid)(implicit driver: WebDriver) = {
    go to PrivateKeeperDetailsPage

    titleDropDown select title
    emailTextBox enter email

    click on next
  }
}
