package pages.acquire

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import uk.gov.dvla.vehicles.presentation.common.views.widgets.MicroServiceError.{ExitId, TryAgainId}

object MicroServiceErrorPage extends Page {
  final val address = buildAppUrl("service-error")
  final override val title = "We are sorry"

  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}