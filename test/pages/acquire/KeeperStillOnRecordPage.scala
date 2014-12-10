package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object KeeperStillOnRecordPage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("keeper-still-on-record")
  override def url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "A keeper is on the record"
}
