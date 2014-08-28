package pages.acquire

import helpers.webbrowser._
import views.acquire.VehicleLookup
import VehicleLookup.{BackId, SubmitId}
import viewmodels.VehicleLookupFormViewModel.Form.{DocumentReferenceNumberId, VehicleRegistrationNumberId}
import webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}

object BusinessKeeperDetailsPage extends Page with WebBrowserDSL {
  final val address = "/vrm-acquire/individual-keeper-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter Keeper Details"
}
