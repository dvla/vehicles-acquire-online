package pages.acquire

import helpers.webbrowser.{Element, Page, SingleSel, WebBrowserDSL, WebDriverFactory}
import viewmodels.BusinessChooseYourAddressViewModel.Form.AddressSelectId
import views.acquire.BusinessChooseYourAddress
import BusinessChooseYourAddress.BackId
import BusinessChooseYourAddress.EnterAddressManuallyButtonId
import BusinessChooseYourAddress.SelectId
import org.openqa.selenium.WebDriver
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid

object BusinessChooseYourAddressPage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/business-choose-your-address"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Select trader address"
  final val titleCy = "Dewiswch eich cyfeiriad masnach"

  def chooseAddress(implicit driver: WebDriver): SingleSel = singleSel(id(AddressSelectId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(EnterAddressManuallyButtonId)).get

  def getList(implicit driver: WebDriver) = singleSel(id(AddressSelectId)).getOptions

  def getListCount(implicit driver: WebDriver): Int = getList.size

  def select(implicit driver: WebDriver): Element = find(id(SelectId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    chooseAddress.value = traderUprnValid.toString
    click on select
  }

  def sadPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    click on select
  }
}