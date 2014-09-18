package helpers.acquire

import uk.gov.dvla.vehicles.presentation.common.model.{VehicleDetailsModel, TraderDetailsModel, AddressModel}
import org.openqa.selenium.{Cookie, WebDriver}
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{Json, Writes}
import uk.gov.dvla.vehicles.presentation.common.controllers.AlternateLanguages.{CyId, EnId}
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.SetupTradeDetailsFormModel.SetupTradeDetailsCacheKey
import TraderDetailsModel.TraderDetailsCacheKey
import models._
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import pages.acquire.SetupTradeDetailsPage.{PostcodeValid, TraderBusinessNameValid, TraderEmailValid}
import webserviceclients.fakes.FakeAddressLookupService.addressWithoutUprn
import webserviceclients.fakes.FakeVehicleLookupWebService._
import pages.acquire.PrivateKeeperDetailsPage._
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel._
import uk.gov.dvla.vehicles.presentation.common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import scala.Some
import webserviceclients.fakes.FakeVehicleLookupWebService.VehicleMakeValid
import models.PrivateKeeperDetailsFormModel._
import scala.Some

object CookieFactoryForUISpecs {
  private def addCookie[A](key: String, value: A)(implicit tjs: Writes[A], webDriver: WebDriver): Unit = {
    val valueAsString = Json.toJson(value).toString()
    val manage = webDriver.manage()
    val cookie = new Cookie(key, valueAsString)
    manage.addCookie(cookie)
  }

  def withLanguageCy()(implicit webDriver: WebDriver) = {
    val key = Play.langCookieName
    val value = CyId
    addCookie(key, value)
    this
  }

  def withLanguageEn()(implicit webDriver: WebDriver) = {
    val key = Play.langCookieName
    val value = EnId
    addCookie(key, value)
    this
  }

  def setupTradeDetails(traderPostcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = SetupTradeDetailsCacheKey
    val value = SetupTradeDetailsFormModel(traderBusinessName = TraderBusinessNameValid,
      traderPostcode = traderPostcode, traderEmail = Some(TraderEmailValid))
    addCookie(key, value)
    this
  }

  def businessChooseYourAddress(uprn: Long = traderUprnValid)(implicit webDriver: WebDriver) = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprn.toString)
    addCookie(key, value)
    this
  }

  def enterAddressManually()(implicit webDriver: WebDriver) = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyFormModel(addressAndPostcodeModel = AddressAndPostcodeViewModel(
      addressLinesModel = AddressLinesViewModel(buildingNameOrNumber = BuildingNameOrNumberValid,
        line2 = Some(Line2Valid),
        line3 = Some(Line3Valid),
        postTown = PostTownValid)))
    addCookie(key, value)
    this
  }

  def dealerDetails(address: AddressModel = addressWithoutUprn)(implicit webDriver: WebDriver) = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(traderName = TraderBusinessNameValid, traderAddress = address)
    addCookie(key, value)
    this
  }

  def vehicleDetails(registrationNumber: String = RegistrationNumberValid,
                     vehicleMake: String = VehicleMakeValid,
                     vehicleModel: String = ModelValid,
                     disposeFlag: Boolean = false)(implicit webDriver: WebDriver) = {
    val key = VehicleLookupDetailsCacheKey
    val value = VehicleDetailsModel(registrationNumber = registrationNumber,
                                    vehicleMake,
                                    vehicleModel,
                                    disposeFlag)
    addCookie(key, value)
    this
  }

  def privateKeeperDetails(title: String = TitleValid,
                           firstName: String = FirstNameValid,
                           lastName: String = LastNameValid,
                           email: Option[String] = Some(EmailValid))(implicit webDriver: WebDriver) = {
    val key = PrivateKeeperDetailsCacheKey
    val value = PrivateKeeperDetailsFormModel(
      title = title,
      firstName = firstName,
      lastName = lastName,
      email = email
    )
    addCookie(key, value)
    this
  }
}
