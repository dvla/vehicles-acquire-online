package pages.acquire

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.acquire.AcquireFailure.BuyAnotherId

object AcquireFailurePage extends Page {
  final val address = s"$applicationContext/buy-from-the-trade-failure"
  final override val title: String = "Selling a vehicle out of trade: failure"
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def buyAnother(implicit driver: WebDriver): Element = find(id(BuyAnotherId)).get
}