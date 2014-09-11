package pages.acquire

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory, Element, SingleSel, EmailField, TextField}
import org.openqa.selenium.WebDriver
import views.acquire.PrivateKeeperDetails.{BackId, SubmitId}
import viewmodels.PrivateKeeperDetailsViewModel.Form.{TitleId, EmailId, FirstNameId, SurnameId}

object PrivateKeeperDetailsPage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/private-keeper-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter keeper details"

  final val TitleValid = "Mrs"
  final val TitleInvalid = ""
  final val OptionValid = "firstOption"
  final val FirstNameValid = "TestFirstName"
  final val FirstNameInvalid = ""
  final val SurnameValid = "TestSurname"
  final val SurnameInvalid = ""
  final val EmailValid = "my@email.com"
  final val EmailInvalid = "no_at_symbol.com"
  final val TitleInvalidError = "Please select a title from the drop down list."
  final val VehicleMakeValid = "Audi"
  final val ModelValid = "A6"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def titleDropDown(implicit driver: WebDriver): SingleSel = singleSel(id(TitleId))

  def emailTextBox(implicit driver: WebDriver): EmailField = emailField(id(EmailId))

  def firstNameTextBox(implicit driver: WebDriver): TextField = textField(id(FirstNameId))

  def surnameTextBox(implicit driver: WebDriver): TextField = textField(id(SurnameId))


  def navigate(title: String = OptionValid,
                firstName: String = FirstNameValid,
                surname: String = SurnameValid,
                email: String = EmailValid)(implicit driver: WebDriver) = {
    go to PrivateKeeperDetailsPage

    titleDropDown select title
    firstNameTextBox enter firstName
    surnameTextBox enter surname
    emailTextBox enter email

    click on next
  }
}
