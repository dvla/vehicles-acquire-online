package pages.acquire

import org.openqa.selenium.WebDriver
import uk.gov.dvla.vehicles.presentation.common
import common.helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory, TextField, Element, RadioButton, EmailField}
import common.mappings.Email.{EmailId, EmailVerifyId}
import common.mappings.OptionalToggle.{Invisible, Visible}
import common.model.SetupTradeDetailsFormModel.Form.{TraderNameId, TraderPostcodeId, TraderEmailId, TraderEmailOptionId}
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

  def traderEmail(implicit driver: WebDriver): EmailField = emailField(id(s"${TraderEmailId}_$EmailId"))

  def traderConfirmEmail(implicit driver: WebDriver): EmailField = emailField(id(s"${TraderEmailId}_$EmailVerifyId"))

  def lookup(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def emailVisible(implicit driver: WebDriver): RadioButton =
    radioButton(id(s"${TraderEmailOptionId}_$Visible"))

  def emailInvisible(implicit driver: WebDriver): RadioButton =
    radioButton(id(s"${TraderEmailOptionId}_$Invisible"))


  def happyPath(traderBusinessName: String = TraderBusinessNameValid,
                traderBusinessPostcode: String = PostcodeValid,
                traderBusinessEmail: Option[String] = Some(TraderEmailValid))
               (implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName enter traderBusinessName
    traderPostcode enter traderBusinessPostcode
    traderBusinessEmail.fold(click on emailInvisible) { email =>
      click on emailVisible
      traderEmail enter email
      traderConfirmEmail enter email
    }
    click on lookup
  }

  def submitPostcodeWithoutAddresses(implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName enter TraderBusinessNameValid
    traderPostcode enter PostcodeWithoutAddresses
    click on emailInvisible
    click on lookup
  }
}
