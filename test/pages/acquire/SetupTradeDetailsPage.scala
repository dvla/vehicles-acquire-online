package pages.acquire

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.TextField
import org.scalatest.selenium.WebBrowser.textField
import org.scalatest.selenium.WebBrowser.EmailField
import org.scalatest.selenium.WebBrowser.emailField
import org.scalatest.selenium.WebBrowser.RadioButton
import org.scalatest.selenium.WebBrowser.radioButton
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common
import common.helpers.webbrowser.{Page, WebDriverFactory}
import common.mappings.Email.{EmailId, EmailVerifyId}
import common.mappings.OptionalToggle.{Invisible, Visible}
import common.model.SetupTradeDetailsFormModel.Form.{TraderNameId, TraderPostcodeId, TraderEmailId, TraderEmailOptionId}
import views.acquire.SetupTradeDetails.SubmitId

object SetupTradeDetailsPage extends Page {
  final val TraderBusinessNameValid = "example trader name"
  final val PostcodeWithoutAddresses = "xx99xx"
  final val PostcodeValid = "QQ99QQ"
  final val TraderEmailValid = "example@example.co.uk"

  private final val route = "setup-trade-details"

  final val address = buildAppUrl(route)

  lazy val cegUrl: String = WebDriverFactory.testUrl + buildAppUrl(s"${route}/ceg").substring(1)
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
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
    traderName.value = traderBusinessName
    traderPostcode.value = traderBusinessPostcode
    traderBusinessEmail.fold(click on emailInvisible) { email =>
      click on emailVisible
      traderEmail.value = email
      traderConfirmEmail.value = email
    }
    click on lookup
  }

  def submitPostcodeWithoutAddresses(implicit driver: WebDriver) = {
    go to SetupTradeDetailsPage
    traderName.value = TraderBusinessNameValid
    traderPostcode.value = PostcodeWithoutAddresses
    click on emailInvisible
    click on lookup
  }
}
