package pages.common

import org.openqa.selenium.WebDriver
import pages.acquire.buildAppUrl
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.common.UprnNotFound.{ManualaddressbuttonId, SetuptradedetailsbuttonId}

object UprnNotFoundPage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("uprn-not-found")
  override def url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Error confirming post code"

  def setupTradeDetails(implicit driver: WebDriver): Element = find(id(SetuptradedetailsbuttonId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(ManualaddressbuttonId)).get
}
