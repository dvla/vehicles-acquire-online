package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.After
import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.PhantomJsDefaultDriver

final class WebBrowserHooks(webBrowserDriver: PhantomJsDefaultDriver) {

  @After
  def quitBrowser() = {
    implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]
    webDriver.quit()
  }
}
