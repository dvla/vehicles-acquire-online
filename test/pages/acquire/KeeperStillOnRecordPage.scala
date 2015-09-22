package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}

object KeeperStillOnRecordPage extends Page {
  final val address = buildAppUrl("keeper-still-on-record")
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "A keeper is on the record"
}
