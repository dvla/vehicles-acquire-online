package pages.acquire

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import views.acquire.AcquireSuccess.BuyAnotherId

object AcquireSuccessPage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("acquire-success")
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Summary"
//  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def buyAnother(implicit driver: WebDriver): Element = find(id(BuyAnotherId)).get
}
