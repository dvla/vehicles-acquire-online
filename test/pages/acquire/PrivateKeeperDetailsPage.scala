package pages.acquire

import helpers.webbrowser.{Element, EmailField, Page, TelField, TextField, WebBrowserDSL, WebDriverFactory}
import models.PrivateKeeperDetailsFormModel.Form.{DateOfBirthId, DriverNumberId, EmailId, FirstNameId, LastNameId, PostcodeId, TitleId}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import play.api.i18n.Messages
import uk.gov.dvla.vehicles.presentation.common.mappings.TitlePickerString
import TitlePickerString.{standardOptions, standardOptionsMessages}
import views.acquire.PrivateKeeperDetails.{BackId, SubmitId}

object PrivateKeeperDetailsPage extends Page with WebBrowserDSL with Matchers {
  final val address = buildAppUrl("private-keeper-details")
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter keeper details"

  final val TitleInvalid = "&^%&%&"
  final val FirstNameValid = "TestFirstName"
  final val FirstNameInvalid = ""
  final val LastNameValid = "TestLastName"
  final val LastNameInvalid = ""
  final val EmailValid = "my@email.com"
  final val EmailInvalid = "no_at_symbol.com"
  final val VehicleMakeValid = "Audi"
  final val ModelValid = "A6"
  final val DriverNumberValid = "ABCD9711215EFLGH"
  final val DriverNumberInvalid = "A"
  final val DayDateOfBirthValid = "24"
  final val MonthDateOfBirthValid = "12"
  final val YearDateOfBirthValid = "1920"
  final val PostcodeValid = "QQ99QQ"
  final val NoPostcodeFound = "XX99XX"
  final val PostcodeInvalid = "XX99X"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def mr(implicit driver: WebDriver) = radioButton(id(s"${TitleId}_titleOption_titlePicker.mr"))
  def miss(implicit driver: WebDriver) = radioButton(id(s"${TitleId}_titleOption_titlePicker.miss"))
  def mrs(implicit driver: WebDriver) = radioButton(id(s"${TitleId}_titleOption_titlePicker.mrs"))
  def other(implicit driver: WebDriver) = radioButton(id(s"${TitleId}_titleOption_titlePicker.other"))
  def otherText(implicit driver: WebDriver) = textField(id(s"${TitleId}_titleText"))

  def emailTextBox(implicit driver: WebDriver): EmailField = emailField(id(EmailId))

  def driverNumberTextBox(implicit driver: WebDriver): TextField = textField(id(DriverNumberId))

  def firstNameTextBox(implicit driver: WebDriver): TextField = textField(id(FirstNameId))

  def lastNameTextBox(implicit driver: WebDriver): TextField = textField(id(LastNameId))

  def dayDateOfBirthTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfBirthId" + "_day"))

  def monthDateOfBirthTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfBirthId" + "_month"))

  def yearDateOfBirthTextBox(implicit driver: WebDriver): TelField = telField(id(s"$DateOfBirthId" + "_year"))

  def postcodeTextBox(implicit driver: WebDriver): TextField = textField(id(PostcodeId))

  private def titleRadioButtons(implicit driver: WebDriver) = Seq(mr, miss, mrs, other)

  def selectTitle(title: String)(implicit driver: WebDriver): Unit =
    if (!title.isEmpty && (standardOptions.contains(title) || standardOptionsMessages.contains(title)))
      titleRadioButtons.find(_.value endsWith title).fold(throw new Exception)(click on _)

  def assertTitleSelected(title: String)(implicit driver: WebDriver): Unit = {
    titleRadioButtons.find(_.underlying.getAttribute("id") endsWith title)
      .fold(throw new Exception) ( _.isSelected should equal(true))
    titleRadioButtons.filterNot(_.underlying.getAttribute("id") endsWith title)
      .map(_.isSelected should equal(false))
  }

  def assertNoTitleSelected()(implicit driver: WebDriver): Unit = {
    titleRadioButtons.foreach(_.isSelected should equal(false))
  }

  def navigate(title: String = standardOptions(0),
                firstName: String = FirstNameValid,
                lastName: String = LastNameValid,
                dayDateOfBirth: String = DayDateOfBirthValid,
                monthDateOfBirth: String = MonthDateOfBirthValid,
                yearDateOfBirth: String = YearDateOfBirthValid,
                email: String = EmailValid,
                driverNumber: String = DriverNumberValid,
                postcode: String = PostcodeValid)(implicit driver: WebDriver) = {
    go to PrivateKeeperDetailsPage
    
    selectTitle(title)
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

  def submitPostcodeWithoutAddresses(implicit driver: WebDriver) = {
    navigate(postcode = NoPostcodeFound)
  }
}
