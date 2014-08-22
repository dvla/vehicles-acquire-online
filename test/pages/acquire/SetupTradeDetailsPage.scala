package pages.acquire

import helpers.webbrowser._
import org.openqa.selenium.WebDriver
import viewmodels.SetupTradeDetailsViewModel.Form._
import views.acquire.SetupTradeDetails._

object SetupTradeDetailsPage extends Page with WebBrowserDSL {
  final val TraderBusinessNameValid = "example trader name"
  final val PostcodeWithoutAddresses = "xx99xx"
  final val PostcodeValid = "QQ99QQ"
  final val TraderEmailValid = "example@example.co.uk"

  final val address = s"/$basePath/setup-trade-details"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
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
