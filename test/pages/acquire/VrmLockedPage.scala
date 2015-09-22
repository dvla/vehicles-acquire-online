package pages.acquire

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.acquire.VrmLocked
import VrmLocked.{ExitId, BuyAnotherVehicleId}

object VrmLockedPage extends Page {
  final val address = s"$applicationContext/vrm-locked"

  override val url: String = WebDriverFactory.testUrl + address.substring(1)

  final override val title = "Registration number is locked"

  def newDisposal(implicit driver: WebDriver): Element = find(id(BuyAnotherVehicleId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
