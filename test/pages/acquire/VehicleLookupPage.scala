package pages.acquire

import helpers.webbrowser.{Element, Page, RadioButton, WebBrowserDSL, WebDriverFactory, TelField, TextField}
import views.acquire.VehicleLookup
import VehicleLookup.{BackId, SubmitId}
import models.VehicleLookupFormModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId, VehicleSoldToId}
import org.openqa.selenium.WebDriver
import webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}
import views.acquire.VehicleLookup.{VehicleSoldTo_Private, VehicleSoldTo_Business}

object VehicleLookupPage extends Page with WebBrowserDSL {
  final val address = s"$basePath/vehicle-lookup"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter vehicle details"

  def vehicleRegistrationNumber(implicit driver: WebDriver): TextField = textField(id(VehicleRegistrationNumberId))

  def documentReferenceNumber(implicit driver: WebDriver): TelField = telField(id(DocumentReferenceNumberId))

  def vehicleSoldToPrivateIndividual(implicit driver: WebDriver): RadioButton = radioButton(id(s"${VehicleSoldToId}_$VehicleSoldTo_Private"))

  def vehicleSoldToBusiness(implicit driver: WebDriver): RadioButton = radioButton(id(s"${VehicleSoldToId}_$VehicleSoldTo_Business"))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(referenceNumber: String = ReferenceNumberValid,
                registrationNumber: String = RegistrationNumberValid,
                isVehicleSoldToPrivateIndividual: Boolean = true)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber enter referenceNumber
    VehicleLookupPage.vehicleRegistrationNumber enter registrationNumber

    if (isVehicleSoldToPrivateIndividual) click on vehicleSoldToPrivateIndividual
    else click on vehicleSoldToBusiness

    click on next
  }
}
