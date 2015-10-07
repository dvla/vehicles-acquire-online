package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import org.scalatest.selenium.WebBrowser.SingleSel
import org.scalatest.selenium.WebBrowser.singleSel
import uk.gov.dvla.vehicles.presentation.common.model.NewKeeperChooseYourAddressFormModel.Form.AddressSelectId
import views.acquire.NewKeeperChooseYourAddress
import NewKeeperChooseYourAddress.BackId
import NewKeeperChooseYourAddress.EnterAddressManuallyButtonId
import NewKeeperChooseYourAddress.SelectId
import org.openqa.selenium.WebDriver

object NewKeeperChooseYourAddressPage extends Page {
  final val address = buildAppUrl("new-keeper-choose-your-address")
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "Select new keeper address"
  val selectedAddressLine = "presentationProperty stub, 123, property stub, street stub, town stub, area stub, QQ99QQ"

  //final val titleCy = "Dewiswch eich cyfeiriad masnach"

  def chooseAddress(implicit driver: WebDriver): SingleSel = singleSel(id(AddressSelectId))

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def manualAddress(implicit driver: WebDriver): Element = find(id(EnterAddressManuallyButtonId)).get

  def select(implicit driver: WebDriver): Element = find(id(SelectId)).get

  def happyPath(implicit driver: WebDriver) = {
    go to NewKeeperChooseYourAddressPage
    chooseAddress.value = selectedAddressLine
    click on select
  }

  def sadPath(implicit driver: WebDriver) = {
    go to NewKeeperChooseYourAddressPage
    click on select
  }
}
