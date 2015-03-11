package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.acquire.VehicleLookupFailure
import VehicleLookupFailure.{BeforeYouStartId, VehicleLookupId}
import org.openqa.selenium.WebDriver

object VehicleLookupFailurePage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("vehicle-lookup-failure")
  final override val title: String = "Unable to find a vehicle record"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def beforeYouStart(implicit driver: WebDriver): Element = find(id(BeforeYouStartId)).get

  def vehicleLookup(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get
}