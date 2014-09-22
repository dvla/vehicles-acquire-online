package pages.acquire

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object KeeperStillOnRecordPage extends Page with WebBrowserDSL {
  final val address = s"$applicationContext/keeper-still-on-record"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "A keeper is on the record"
}
