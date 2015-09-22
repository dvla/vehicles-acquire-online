package pages.acquire

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.acquire.AcquireSuccess.BuyAnotherId

object AcquireSuccessPage extends Page {
  final val address = buildAppUrl("acquire-success")
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Summary"
//  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def buyAnother(implicit driver: WebDriver): Element = find(id(BuyAnotherId)).get
}
