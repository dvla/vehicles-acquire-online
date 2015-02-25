package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.acquire.AcquireFailure
import AcquireFailure.BuyAnotherId
import org.openqa.selenium.WebDriver

object AcquireFailurePage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/buy-from-the-trade-failure"
  final override val title: String = "Buy a vehicle from the motor trade: failure"
  override def url: String = WebDriverFactory.testUrl + address.substring(1)

  def buyAnother(implicit driver: WebDriver): Element = find(id(BuyAnotherId)).get
}