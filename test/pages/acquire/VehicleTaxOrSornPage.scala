package pages.acquire

import models.VehicleTaxOrSornFormModel.Form.{TaxId, SornId}
import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.acquire.VehicleTaxOrSorn.{BackId, SubmitId}

object VehicleTaxOrSornPage extends Page with WebBrowserDSL {
  final val address = buildAppUrl("vehicle-tax-or-sorn")
  override def url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Vehicle tax or SORN"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def sornSelect(implicit driver: WebDriver): Element = find(id(SornId)).get

  def taxSelect(implicit driver: WebDriver): Element = find(id(TaxId)).get

  def navigate()(implicit driver: WebDriver) = {
    go to VehicleTaxOrSornPage
    click on sornSelect
    click on next
  }
}
