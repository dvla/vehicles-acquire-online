package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import views.acquire.BeforeYouStart
import BeforeYouStart.NextId
import org.openqa.selenium.WebDriver

object BeforeYouStartPage extends Page {
  final val address = buildAppUrl("before-you-start")
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Selling a vehicle out of trade"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}
