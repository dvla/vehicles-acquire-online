package pages.acquire

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.acquire.VehicleLookupFailure
import VehicleLookupFailure.{BeforeYouStartId, VehicleLookupId}
import org.openqa.selenium.WebDriver

object VehicleLookupFailurePage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("vehicle-lookup-failure")
  final override val title: String = "Look-up was unsuccessful"

  override val url: String = WebDriverFactory.testUrl + address.substring(1)

  def beforeYouStart(implicit driver: WebDriver): Element = find(id(BeforeYouStartId)).get

  def vehicleLookup(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get
}