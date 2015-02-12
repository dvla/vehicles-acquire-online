package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Element, Page, TextField, WebBrowserDSL, WebDriverFactory}
import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common
import common.model.SetupTradeDetailsFormModel.Form.{TraderNameId, TraderPostcodeId, TraderEmailId}
import views.acquire.SetupTradeDetails.SubmitId

object SetupTradeDetailsPage extends Page with WebBrowserDSL {
  final val TraderBusinessNameValid = "example trader name"
  final val PostcodeWithoutAddresses = "xx99xx"
  final val PostcodeValid = "QQ99QQ"
  final val TraderEmailValid = "example@example.co.uk"

  final val address = buildAppUrl("setup-trade-details")
  override def url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Provide trader details"


  def traderName(implicit driver: WebDriver): TextField = textField(id(TraderNameId))

  def traderPostcode(implicit driver: WebDriver): TextField = textField(id(TraderPostcodeId))

  def traderEmail(implicit driver: WebDriver): TextField = textField(id(TraderEmailId))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get


  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessPostcode: String = PostcodeValid,
                traderBusinessEmail: String = TraderEmailValid)
               (implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName enter traderBusinessName
    traderPostcode enter traderBusinessPostcode
    traderEmail enter traderBusinessEmail
    click on lookup
  }

  def submitPostcodeWithoutAddresses(implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName enter TraderBusinessNameValid
    traderPostcode enter PostcodeWithoutAddresses
    click on lookup
  }
}
