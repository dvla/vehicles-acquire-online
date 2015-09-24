package pages.acquire

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.acquire.VehicleLookupFailure
import VehicleLookupFailure.{BeforeYouStartId, VehicleLookupId}

object VehicleLookupFailurePage extends Page {
  final val address = buildAppUrl("vehicle-lookup-failure")
  final override val title: String = "Unable to find a vehicle record"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def beforeYouStart(implicit driver: WebDriver): Element = find(id(BeforeYouStartId)).get

  def vehicleLookup(implicit driver: WebDriver): Element = find(id(VehicleLookupId)).get
}