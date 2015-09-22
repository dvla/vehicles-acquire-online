package pages.acquire

import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{Page, WebDriverFactory}
import models.EnterAddressManuallyFormModel.Form.AddressAndPostcodeId
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.TextField
import org.scalatest.selenium.WebBrowser.textField
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.AddressLinesId
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.BuildingNameOrNumberId
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.Line2Id
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.Line3Id
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.PostTownId
import views.acquire.EnterAddressManually.{BackId, NextId}
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}

object EnterAddressManuallyPage extends Page {
  final val address = buildAppUrl("enter-address-manually")
  override val url: String = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Enter address"

  def addressBuildingNameOrNumber(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$BuildingNameOrNumberId"))

  def addressLine2(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$Line2Id"))

  def addressLine3(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$Line3Id"))

  def addressPostTown(implicit driver: WebDriver): TextField =
    textField(id(s"${AddressAndPostcodeId}_${AddressLinesId}_$PostTownId"))

  def next(implicit driver: WebDriver): Element = find(id(NextId)).get

  def back(implicit driver: WebDriver): Element = find(id(BackId)).get

  def happyPath(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                line2: String = Line2Valid,
                line3: String = Line3Valid,
                postTown: String = PostTownValid)
               (implicit driver: WebDriver) = {
    go to EnterAddressManuallyPage
    addressBuildingNameOrNumber.value = buildingNameOrNumber
    addressLine2.value = line2
    addressLine3.value = line3
    addressPostTown.value = postTown
    click on next
  }

  def happyPathMandatoryFieldsOnly(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                                   postTown: String = PostTownValid)
                                  (implicit driver: WebDriver) = {
    go to EnterAddressManuallyPage
    addressBuildingNameOrNumber.value = buildingNameOrNumber
    addressPostTown.value = postTown
    click on next
  }

  def sadPath(implicit driver: WebDriver) = {
    go to EnterAddressManuallyPage
    click on next
  }
}
