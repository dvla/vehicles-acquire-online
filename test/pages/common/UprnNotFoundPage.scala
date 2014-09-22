package pages.common

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
import views.common.UprnNotFound
import UprnNotFound.{ManualaddressbuttonId, SetuptradedetailsbuttonId}
import org.openqa.selenium.WebDriver

object UprnNotFoundPage extends Page with WebBrowserDSL {
  final val applicationContext = getProperty("application.context", default = "")

  final val address = s"$applicationContext/uprn-not-found"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Error confirming post code"

  def setupTradeDetails(implicit driver: WebDriver): Element = find(id(SetuptradedetailsbuttonId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(ManualaddressbuttonId)).get
}
