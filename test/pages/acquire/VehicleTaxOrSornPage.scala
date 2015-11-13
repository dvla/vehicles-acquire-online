package pages.acquire

import models.VehicleTaxOrSornFormModel.Form.{SelectId, SornId, TaxId}
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{Element, click, find, go, id}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Page, WebDriverFactory}
import views.acquire.VehicleTaxOrSorn.{BackId, SubmitId}

object VehicleTaxOrSornPage extends Page {
  final val address = buildAppUrl("vehicle-tax-or-sorn")
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Vehicle tax or SORN"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def sornSelect(implicit driver: WebDriver): Element = find(id(SelectId + "_" + SornId)).get

  def taxSelect(implicit driver: WebDriver): Element = find(id(SelectId + "_" + TaxId)).get

  def navigate()(implicit driver: WebDriver) = {
    go to VehicleTaxOrSornPage
    click on sornSelect
    click on next
  }
}
