package pages.acquire

import helpers.webbrowser.{Element, EmailField, Page, SingleSel, TelField, TextField, WebBrowserDSL, WebDriverFactory}
import models.PrivateKeeperDetailsFormModel.Form.{DateOfBirthId, DriverNumberId, EmailId, FirstNameId, LastNameId}
import models.PrivateKeeperDetailsFormModel.Form.{TitleId, PostcodeId}
import org.openqa.selenium.WebDriver
import views.acquire.PrivateKeeperDetails.{BackId, SubmitId}

object PrivateKeeperDetailsPage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/private-keeper-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter keeper details"

  final val TitleValid = "Mrs"
  final val TitleInvalid = ""
  final val OptionValid = "firstOption"
  final val FirstNameValid = "TestFirstName"
  final val FirstNameInvalid = ""
  final val LastNameValid = "TestLastName"
  final val LastNameInvalid = ""
  final val EmailValid = "my@email.com"
  final val EmailInvalid = "no_at_symbol.com"
  final val TitleInvalidError = "Please select a title from the drop down list."
  final val VehicleMakeValid = "Audi"
  final val ModelValid = "A6"
  final val DriverNumberValid = "ABCD9711215EFLGH"
  final val DriverNumberInvalid = "A"
  final val DayDateOfBirthValid = "24"
  final val MonthDateOfBirthValid = "12"
  final val YearDateOfBirthValid = "1920"
  final val PostcodeValid = "QQ99QQ"
  final val PostcodeInvalid = "Q9"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def titleDropDown(implicit driver: WebDriver): SingleSel = singleSel(id(TitleId))

  def emailTextBox(implicit driver: WebDriver): EmailField = emailField(id(EmailId))

  def driverNumberTextBox(implicit driver: WebDriver): TextField = textField(id(DriverNumberId))

  def firstNameTextBox(implicit driver: WebDriver): TextField = textField(id(FirstNameId))

  def lastNameTextBox(implicit driver: WebDriver): TextField = textField(id(LastNameId))

  def dayDateOfBirthTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfBirthId" + "_day"))

  def monthDateOfBirthTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfBirthId" + "_month"))

  def yearDateOfBirthTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfBirthId" + "_year"))

  def postcodeTextBox(implicit driver: WebDriver): TextField = textField(id(PostcodeId))

  def navigate(title: String = OptionValid,
                firstName: String = FirstNameValid,
                lastName: String = LastNameValid,
                dayDateOfBirth: String = DayDateOfBirthValid,
                monthDateOfBirth: String = MonthDateOfBirthValid,
                yearDateOfBirth: String = YearDateOfBirthValid,
                email: String = EmailValid,
                driverNumber: String = DriverNumberValid,
                postcode: String = PostcodeValid)(implicit driver: WebDriver) = {
    go to PrivateKeeperDetailsPage

    titleDropDown select title
    firstNameTextBox enter firstName
    lastNameTextBox enter lastName
    dayDateOfBirthTextBox enter dayDateOfBirth
    monthDateOfBirthTextBox enter monthDateOfBirth
    yearDateOfBirthTextBox enter yearDateOfBirth
    emailTextBox enter email
    driverNumberTextBox enter driverNumber
    postcodeTextBox enter postcode

    click on next
  }
}
