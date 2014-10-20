package helpers.acquire

import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.SetupTradeDetailsFormModel.SetupTradeDetailsCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.{AcquireCompletionViewModel, SetupTradeDetailsFormModel, BusinessChooseYourAddressFormModel, EnterAddressManuallyFormModel, VehicleLookupFormModel, PrivateKeeperDetailsFormModel, BusinessKeeperDetailsFormModel, NewKeeperDetailsViewModel, CompleteAndConfirmFormModel}
import models.VehicleLookupFormModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}
import models.VehicleTaxOrSornFormModel
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import org.joda.time.LocalDate
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import pages.acquire.SetupTradeDetailsPage.{PostcodeValid, TraderBusinessNameValid, TraderEmailValid}
import pages.acquire.BusinessKeeperDetailsPage.{FleetNumberValid, BusinessNameValid}
import pages.acquire.PrivateKeeperDetailsPage.{ModelValid, FirstNameValid, LastNameValid, EmailValid, DriverNumberValid}
import pages.acquire.PrivateKeeperDetailsPage.DayDateOfBirthValid
import pages.acquire.PrivateKeeperDetailsPage.MonthDateOfBirthValid
import pages.acquire.PrivateKeeperDetailsPage.YearDateOfBirthValid
import pages.acquire.CompleteAndConfirmPage.{MileageValid,DayDateOfSaleValid,MonthDateOfSaleValid,YearDateOfSaleValid}
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{Json, Writes}
import views.acquire.VehicleLookup.VehicleSoldTo_Private
import uk.gov.dvla.vehicles.presentation.common
import common.controllers.AlternateLanguages.{CyId, EnId}
import common.mappings.TitleType
import common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import common.model.{BruteForcePreventionModel, VehicleDetailsModel, TraderDetailsModel, AddressModel}
import TraderDetailsModel.TraderDetailsCacheKey
import common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.UprnValid
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import webserviceclients.fakes.FakeAddressLookupService.addressWithoutUprn
import webserviceclients.fakes.FakeVehicleLookupWebService.{TransactionIdValid, TransactionTimestampValid, ReferenceNumberValid, RegistrationNumberValid, VehicleMakeValid}

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

  def businessChooseYourAddress(uprn: Long = UprnValid)(implicit webDriver: WebDriver) = {
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

  def privateKeeperDetails(title: TitleType = TitleType(1, ""),
                           firstName: String = FirstNameValid,
                           lastName: String = LastNameValid,
                           email: Option[String] = Some(EmailValid),
                           dateOfBirth: Option[LocalDate] = Some(new LocalDate(
                             YearDateOfBirthValid.toInt,
                             MonthDateOfBirthValid.toInt,
                             DayDateOfBirthValid.toInt
                           )),
                           driverNumber: Option[String] = Some(DriverNumberValid),
                           postcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = PrivateKeeperDetailsCacheKey
    val value = PrivateKeeperDetailsFormModel(
      title = title,
      firstName = firstName,
      lastName = lastName,
      dateOfBirth,
      email = email,
      driverNumber = driverNumber,
      postcode = postcode
    )
    addCookie(key, value)
    this
  }

  def businessKeeperDetails(fleetNumber: Option[String] = Some(FleetNumberValid),
                            businessName: String = BusinessNameValid,
                            email: Option[String] = Some(EmailValid),
                            postcode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = BusinessKeeperDetailsCacheKey
    val value = BusinessKeeperDetailsFormModel(
      fleetNumber = fleetNumber,
      businessName = businessName,
      email = email,
      postcode = postcode
    )
    addCookie(key, value)
    this
  }

  def newKeeperDetails(title: Option[TitleType] = None,
                       firstName: Option[String] = None,
                       lastName: Option[String] = None,
                       dateOfBirth: Option[LocalDate] = None,
                       driverNumber: Option[String] = None,
                       businessName: Option[String] = None,
                       fleetNumber: Option[String] = None,
                       email: Option[String] = None,
                       address: AddressModel = addressWithoutUprn,
                       isBusinessKeeper: Boolean = false)
                      (implicit webDriver: WebDriver) = {
    val key = NewKeeperDetailsCacheKey
    val value = NewKeeperDetailsViewModel(
      title = title,
      firstName = firstName,
      lastName = lastName,
      dateOfBirth = dateOfBirth,
      driverNumber = driverNumber,
      businessName = businessName,
      fleetNumber = fleetNumber,
      address = address,
      email = email,
      isBusinessKeeper = isBusinessKeeper,
      displayName = if (businessName == None) firstName + " " + lastName else businessName.getOrElse("")
    )
    addCookie(key, value)
    this
  }

  def acquireCompletionViewModel()(implicit webDriver: WebDriver) = {

    val key = AcquireCompletionViewModel.AcquireCompletionCacheKey

    val vehicleDetails = VehicleDetailsModel(RegistrationNumberValid, VehicleMakeValid, ModelValid, false)

    val traderDetails = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressModel(
        uprn = None,
        address = Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid, PostcodeValid)
      ),
      None
    )

    val newKeeperDetailsView = NewKeeperDetailsViewModel(None, None, None, None, None, None, None, None,
      AddressModel(None, Seq(BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid, PostcodeValid)),
      false, "")

    val completeAndConfirmForm = CompleteAndConfirmFormModel(None, new LocalDate(), "")

    val vehicleSorn = VehicleTaxOrSornFormModel(sornVehicle = Some("true"))

    val value = AcquireCompletionViewModel(vehicleDetails,
      traderDetails,
      newKeeperDetailsView,
      completeAndConfirmForm,
      vehicleSorn,
      TransactionIdValid,
      TransactionTimestampValid
    )
    addCookie(key, value)
    this
  }

  def vehicleTaxOrSornFormModel(sornVehicle: Option[String] = None)
                            (implicit webDriver: WebDriver) = {
    val key = VehicleTaxOrSornCacheKey
    val value = VehicleTaxOrSornFormModel(sornVehicle = sornVehicle)
    addCookie(key, value)
    this
  }

  def completeAndConfirmDetails(mileage: Option[Int] = Some(MileageValid.toInt),
                              dateOfSale: LocalDate = new LocalDate(
                                YearDateOfSaleValid.toInt,
                                MonthDateOfSaleValid.toInt,
                                DayDateOfSaleValid.toInt))(implicit webDriver: WebDriver) = {
    val key = CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
    val value = CompleteAndConfirmFormModel(
      mileage,
      dateOfSale,
      ""
    )
    addCookie(key, value)
    this
  }
}