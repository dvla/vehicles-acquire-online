package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{RadioButton, Checkbox, Element, Page, WebBrowserDSL, WebDriverFactory}
import models.VehicleTaxOrSornFormModel.Form.{SornVehicleId, SornId}
import org.openqa.selenium.WebDriver
import views.acquire.VehicleTaxOrSorn.{BackId, SubmitId}

object VehicleTaxOrSornPage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("vehicle-tax-or-sorn")
  override def url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Vehicle tax or SORN"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def sornSelect(implicit driver: WebDriver): Element = find(id(SornId)).get

  def navigate()(implicit driver: WebDriver) = {
    go to VehicleTaxOrSornPage
    click on sornSelect
    click on next
  }
}
