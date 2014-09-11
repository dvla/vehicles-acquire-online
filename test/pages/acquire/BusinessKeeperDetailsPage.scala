package pages.acquire

import org.openqa.selenium.WebDriver
import helpers.webbrowser.{Element, EmailField, Page, TextField, WebBrowserDSL, WebDriverFactory}
import viewmodels.BusinessKeeperDetailsFormViewModel.Form.{FleetNumberId, BusinessNameId, EmailId}
import views.acquire.BusinessKeeperDetails.{BackId, NextId}

object BusinessKeeperDetailsPage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/business-keeper-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter business keeper details"

  final val FleetNumberValid = "123456"
  final val BusinessNameValid = "Brand New Motors"
  final val EmailValid = "my@email.com"

  def fleetNumberField(implicit driver: WebDriver): TextField = textField(id(FleetNumberId))

  def businessNameField(implicit driver: WebDriver): TextField = textField(id(BusinessNameId))

  def emailField(implicit driver: WebDriver): EmailField = emailField(id(EmailId))
  
  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(NextId)).get

  def navigate(fleetNumber: String = FleetNumberValid, businessName: String = BusinessNameValid, email: String = EmailValid)(implicit driver: WebDriver) = {
    go to BusinessKeeperDetailsPage

    fleetNumberField enter fleetNumber
    businessNameField enter businessName
    emailField enter email

    click on next
  }
}
