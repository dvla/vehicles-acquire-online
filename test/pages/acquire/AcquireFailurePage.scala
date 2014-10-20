package pages.acquire

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.acquire.AcquireFailure
import AcquireFailure.BuyAnotherId
import org.openqa.selenium.WebDriver
import pages.ApplicationContext.applicationContext

object AcquireFailurePage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/buy-from-the-trade-failure"
  final override val title: String = "Buy a vehicle from the motor trade: failure"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)

  def buyAnother(implicit driver: WebDriver): Element = find(id(BuyAnotherId)).get
}