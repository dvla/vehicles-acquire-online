package pages.acquire

import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.SingleSel
import org.scalatest.selenium.WebBrowser.singleSel
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.Page
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.acquire.BusinessChooseYourAddress
import BusinessChooseYourAddress.{BackId,SubmitId}
import BusinessChooseYourAddress.EnterAddressManuallyButtonId
import BusinessChooseYourAddress.SelectId

object BusinessChooseYourAddressPage extends Page {
  final val address = buildAppUrl("business-choose-your-address")
  final val defaultSelectedAddress = "Not real street 1, Not real street2, Not real town, QQ9 9QQ"
  final val selectedAddress = "presentationProperty stub, 123, property stub, street stub, town stub, area stub, QQ99QQ"
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Select trader address"
  final val titleCy = "Dewiswch eich cyfeiriad masnach"

  def chooseAddress(implicit driver: WebDriver): SingleSel = singleSel(id(AddressSelectId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(EnterAddressManuallyButtonId)).get

  def select(implicit driver: WebDriver): Element = find(id(SelectId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    chooseAddress.value = selectedAddress
    click on select
  }

  def sadPath(implicit driver: WebDriver) = {
    go to BusinessChooseYourAddressPage
    click on select
  }
}
