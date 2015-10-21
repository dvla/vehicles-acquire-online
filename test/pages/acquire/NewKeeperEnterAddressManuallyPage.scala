package pages.acquire

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.TextField
import org.scalatest.selenium.WebBrowser.textField
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common
import common.helpers.webbrowser.{Page, WebDriverFactory}
import common.model.NewKeeperEnterAddressManuallyFormModel.Form.AddressAndPostcodeId
import common.views.models.AddressAndPostcodeViewModel.Form.PostcodeId
import common.views.models.AddressLinesViewModel.Form.{AddressLinesId, BuildingNameOrNumberId, Line2Id, Line3Id, PostTownId}
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}
import views.acquire.EnterAddressManually.{BackId, NextId}

object NewKeeperEnterAddressManuallyPage extends Page {
  final val address = buildAppUrl("new-keeper-enter-address-manually")
  override lazy val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter keeper address"

  def addressBuildingNameOrNumber(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$BuildingNameOrNumberId"))

  def addressLine2(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$Line2Id"))

  def addressLine3(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$Line3Id"))

  def addressPostTown(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$PostTownId"))

  def addressPostCode(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_$PostcodeId"))

  def next(implicit driver: WebDriver): Element = find(id(NextId)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def happyPath(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                line2: String = Line2Valid,
                line3: String = Line3Valid,
                postTown: String = PostTownValid)
               (implicit driver: WebDriver) = {
    go to NewKeeperEnterAddressManuallyPage
    addressBuildingNameOrNumber.value = buildingNameOrNumber
    addressLine2.value = line2
    addressLine3.value = line3
    addressPostTown.value = postTown
    click on next
  }

  def happyPathMandatoryFieldsOnly(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                                   postTown: String = PostTownValid)
                                  (implicit driver: WebDriver) = {
    go to NewKeeperEnterAddressManuallyPage
    addressBuildingNameOrNumber.value = buildingNameOrNumber
    addressPostTown.value = postTown
    click on next
  }

  def sadPath(implicit driver: WebDriver) = {
    go to NewKeeperEnterAddressManuallyPage
    click on next
  }
}
