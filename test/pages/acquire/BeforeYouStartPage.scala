package pages.acquire

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.acquire.BeforeYouStart.NextId

object BeforeYouStartPage extends Page {
  final val address = buildAppUrl("before-you-start")
  final override val title: String = "Selling a vehicle out of trade"
  //final val titleCy: String = TODO uncomment and set when Welsh translation complete

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def startNow(implicit driver: WebDriver) = find(id(NextId)).get
}