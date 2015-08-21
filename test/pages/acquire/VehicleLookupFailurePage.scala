package pages.acquire

import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.acquire.VehicleLookupFailure
import VehicleLookupFailure.{BeforeYouStartId, VehicleLookupId}

object VehicleLookupFailurePage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("vehicle-lookup-failure")
  final override val title: String = "Unable to find a vehicle record"

  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def beforeYouStart(implicit driver: WebDriver): Element = find(id(BeforeYouStartId)).get

  def vehicleLookup(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get
}