package pages.acquire

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory}

object SetupTradeDetailsPage extends Page with WebBrowserDSL {
  final val address = "/vrm-acquire/setup-trade-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide trader details"
}
