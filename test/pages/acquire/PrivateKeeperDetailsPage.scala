package pages.acquire

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object PrivateKeeperDetailsPage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/private-keeper-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter keeper details"
}
