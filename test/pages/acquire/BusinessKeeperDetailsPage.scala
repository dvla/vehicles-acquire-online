package pages.acquire

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object BusinessKeeperDetailsPage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/business-keeper-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter business keeper details"
}
