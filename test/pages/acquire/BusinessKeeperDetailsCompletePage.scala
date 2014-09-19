package pages.acquire

import helpers.webbrowser._
import org.openqa.selenium.WebDriver
import views.acquire.BusinessKeeperDetailsComplete.{BackId, SubmitId}
import models.BusinessKeeperDetailsCompleteFormModel.Form.{MileageId, ConsentId}

object BusinessKeeperDetailsCompletePage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/business-complete-and-confirm"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Complete and confirm"

  final val MileageValid = "1000"

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def next(implicit driver: WebDriver): Element = find(id(SubmitId)).get

  def mileageTextBox(implicit driver: WebDriver): TelField = telField(id(MileageId))

  def navigate(mileage: String = MileageValid, consent: String = ConsentId)(implicit driver: WebDriver) = {
    go to BusinessKeeperDetailsCompletePage

    mileageTextBox enter mileage
    click on consent

    click on next
  }
}
