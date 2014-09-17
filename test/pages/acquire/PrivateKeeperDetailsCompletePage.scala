package pages.acquire

import helpers.webbrowser.{Page, WebBrowserDSL, WebDriverFactory, Element, SingleSel, EmailField, TextField}
import org.openqa.selenium.WebDriver
import views.acquire.PrivateKeeperDetails.{BackId, SubmitId}
import viewmodels.PrivateKeeperDetailsViewModel.Form.{TitleId, EmailId, FirstNameId, LastNameId}

object PrivateKeeperDetailsCompletePage extends Page with WebBrowserDSL {
  final val address = s"/$basePath/private-keeper-details-complete"
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Complete and confirm"

  final val DayDateOfBirthValid = "01"
  final val MonthDateOfBirthValid = "02"
  final val YearDateOfBirthValid = "1920"
  final val MileageValid = "1000"

}
