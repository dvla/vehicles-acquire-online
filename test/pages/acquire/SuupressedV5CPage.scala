package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object SuupressedV5CPage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("suppressed-v5c")
  override def url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "The V5C is suppressed"
}
