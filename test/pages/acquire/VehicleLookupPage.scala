package pages.acquire

import helpers.webbrowser._
import views.acquire.VehicleLookup
import VehicleLookup.{BackId, SubmitId}
import viewmodels.VehicleLookupFormViewModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import org.openqa.selenium.WebDriver
import views.acquire.VehicleLookup._
import webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}

object VehicleLookupPage extends Page with WebBrowserDSL {
  final val address = "/vrm-acquire/vehicle-lookup"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter vehicle details"

  def vehicleRegistrationNumber(implicit driver: WebDriver): TextField = textField(id(VehicleRegistrationNumberId))

  def documentReferenceNumber(implicit driver: WebDriver): TelField = telField(id(DocumentReferenceNumberId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get

  def findVehicleDetails(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(referenceNumber: String = ReferenceNumberValid, registrationNumber: String = RegistrationNumberValid)
               (implicit driver: WebDriver) = {
    go to VehicleLookupPage
    documentReferenceNumber enter referenceNumber
    VehicleLookupPage.vehicleRegistrationNumber enter registrationNumber
    click on findVehicleDetails
  }
}
