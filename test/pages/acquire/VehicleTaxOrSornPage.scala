package pages.acquire

import helpers.webbrowser._
import org.openqa.selenium.WebDriver
import views.acquire.CompleteAndConfirm.{BackId, SubmitId}

object VehicleTaxOrSornPage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("vehicle-tax-or-sorn")
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Vehicle tax or SORN"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def navigate()(implicit driver: WebDriver) = {
    go to VehicleTaxOrSornPage
    click on next
  }
}

