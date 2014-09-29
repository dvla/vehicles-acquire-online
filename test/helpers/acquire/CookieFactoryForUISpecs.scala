package helpers.acquire

import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.SetupTradeDetailsFormModel.SetupTradeDetailsCacheKey
import models.{SetupTradeDetailsFormModel, BusinessChooseYourAddressFormModel, EnterAddressManuallyFormModel}
import models.{VehicleLookupFormModel, PrivateKeeperDetailsFormModel, BusinessKeeperDetailsFormModel}
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.VehicleLookupFormModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}
import org.openqa.selenium.{Cookie, WebDriver}
import pages.acquire.SetupTradeDetailsPage.{PostcodeValid, TraderBusinessNameValid, TraderEmailValid}
import pages.acquire.BusinessKeeperDetailsPage.{FleetNumberValid, BusinessNameValid}
import pages.acquire.PrivateKeeperDetailsPage.{ModelValid, TitleValid, FirstNameValid, LastNameValid, EmailValid, DriverNumberValid}
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{Json, Writes}
import views.acquire.VehicleLookup.VehicleSoldTo_Private
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}
import uk.gov.dvla.vehicles.presentation.common
import common.controllers.AlternateLanguages.{CyId, EnId}
import common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import common.model.{BruteForcePreventionModel, VehicleDetailsModel, TraderDetailsModel, AddressModel}
import TraderDetailsModel.TraderDetailsCacheKey
import common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import webserviceclients.fakes.FakeAddressLookupService.addressWithoutUprn
import webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid}
import webserviceclients.fakes.FakeVehicleLookupWebService.VehicleMakeValid

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

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString)
                                   (implicit webDriver: WebDriver) = {
    val key = BruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology
    )
    addCookie(key, value)
    this
  }

  def vehicleLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                             registrationNumber: String = RegistrationNumberValid,
                             vehicleSoldTo: String = VehicleSoldTo_Private)
                            (implicit webDriver: WebDriver) = {
    val key = VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      vehicleSoldTo = vehicleSoldTo)
    addCookie(key, value)
    this
  }

  def vehicleLookupResponseCode(responseCode: String = "disposal_vehiclelookupfailure")
                               (implicit webDriver: WebDriver) = {
    val key = VehicleLookupResponseCodeCacheKey
    val value = responseCode
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
                           email: Option[String] = Some(EmailValid),
                           driverNumber: Option[String] = Some(DriverNumberValid))(implicit webDriver: WebDriver) = {
    val key = PrivateKeeperDetailsCacheKey
    val value = PrivateKeeperDetailsFormModel(
      title = title,
      firstName = firstName,
      lastName = lastName,
      email = email,
      driverNumber = driverNumber
    )
    addCookie(key, value)
    this
  }

  def businessKeeperDetails(fleetNumber: Option[String] = Some(FleetNumberValid),
                            businessName: String = BusinessNameValid,
                            email: Option[String] = Some(EmailValid))(implicit webDriver: WebDriver) = {
    val key = BusinessKeeperDetailsCacheKey
    val value = BusinessKeeperDetailsFormModel(
      fleetNumber = fleetNumber,
      businessName = businessName,
      email = email
    )
    addCookie(key, value)
    this
  }
}
