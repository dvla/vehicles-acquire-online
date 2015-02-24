package pages.acquire

import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Element, Page, TextField, TelField, WebBrowserDSL, WebDriverFactory}
import uk.gov.dvla.vehicles.presentation.common
import common.model.BusinessKeeperDetailsFormModel.Form.{FleetNumberId, BusinessNameId, EmailId, PostcodeId}
import views.acquire.BusinessKeeperDetails.{BackId, NextId}

object BusinessKeeperDetailsPage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("business-keeper-details")
  override def url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter new keeper details"

  final val FleetNumberValid = "123456"
  final val BusinessNameValid = "Brand New Motors"
  final val EmailValid = "my@email.com"
  final val PostcodeValid = "QQ99QQ"
  final val PostcodeInvalid = "XX99XX"

  def fleetNumberField(implicit driver: WebDriver): TelField = telField(id(FleetNumberId))

  def businessNameField(implicit driver: WebDriver): TextField = textField(id(BusinessNameId))

  def emailField(implicit driver: WebDriver): TextField = textField(id(EmailId))

  def postcodeField(implicit driver: WebDriver): TextField = textField(id(PostcodeId))
  
  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(NextId)).get

  def navigate(fleetNumber: String = FleetNumberValid,
               businessName: String = BusinessNameValid,
               email: String = EmailValid,
               postcode: String = PostcodeValid)(implicit driver: WebDriver) = {
    go to BusinessKeeperDetailsPage

    fleetNumberField enter fleetNumber
    businessNameField enter businessName
    emailField enter email
    postcodeField enter postcode

    click on next
  }

  def submitPostcodeWithoutAddresses(implicit driver: WebDriver) = {
    navigate(postcode = PostcodeInvalid)
  }
}