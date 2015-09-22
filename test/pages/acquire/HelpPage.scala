package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import views.common.Help.{BackId, ExitId}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element

object HelpPage extends Page {
  final val address = buildAppUrl("help")
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Help"

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get
}