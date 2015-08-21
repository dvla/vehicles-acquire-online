package pages.acquire

import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.acquire.AcquireFailure.BuyAnotherId

object AcquireFailurePage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/buy-from-the-trade-failure"
  final override val title: String = "Selling a vehicle out of trade: failure"
  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def buyAnother(implicit driver: WebDriver): Element = find(id(BuyAnotherId)).get
}