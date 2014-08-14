package pages.acquire

import helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.acquire.BeforeYouStart
import BeforeYouStart.NextId
import org.openqa.selenium.WebDriver

object BeforeYouStartPage extends Page with WebBrowserDSL {
  final val address = "/vrm-acquire/before-you-start"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Buy a vehicle from the motor trade"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}
