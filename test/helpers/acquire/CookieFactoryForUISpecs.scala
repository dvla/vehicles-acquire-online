package helpers.acquire

import helpers.acquire.CookieFactoryForUnitSpecs.{ConsentTrue, VehicleLookupFailureResponseMessage}
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.{BusinessChooseYourAddressFormModel, CompleteAndConfirmFormModel, CompleteAndConfirmResponseModel, EnterAddressManuallyFormModel}
import models.{IdentifierCacheKey, VehicleLookupFormModel, VehicleTaxOrSornFormModel}
import models.VehicleTaxOrSornFormModel.Form.SornId
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import org.joda.time.{DateTime, LocalDate}
import org.openqa.selenium.{Cookie, WebDriver}
import pages.acquire.BusinessKeeperDetailsPage.{BusinessNameValid, FleetNumberValid}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MileageValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import pages.acquire.PrivateKeeperDetailsPage.{DayDateOfBirthValid, DriverNumberValid, EmailValid, FirstNameValid, LastNameValid, MonthDateOfBirthValid, YearDateOfBirthValid}
import pages.acquire.SetupTradeDetailsPage.{PostcodeValid, TraderBusinessNameValid}
import play.api.Play
import play.api.Play.current
import play.api.libs.json.{Json, Writes}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.controllers.AlternateLanguages.{CyId, EnId}
import common.mappings.TitleType
import common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import common.model.BusinessKeeperDetailsFormModel.businessKeeperDetailsCacheKey
import common.model.MicroserviceResponseModel
import common.model.MicroserviceResponseModel.MsResponseCacheKey
import common.model.NewKeeperDetailsViewModel.newKeeperDetailsCacheKey
import common.model.NewKeeperEnterAddressManuallyFormModel.newKeeperEnterAddressManuallyCacheKey
import common.model.PrivateKeeperDetailsFormModel.privateKeeperDetailsCacheKey
import common.model.SetupTradeDetailsFormModel.setupTradeDetailsCacheKey
import common.model.TraderDetailsModel.traderDetailsCacheKey
import common.model.{AddressModel, BruteForcePreventionModel, BusinessKeeperDetailsFormModel}
import common.model.{NewKeeperDetailsViewModel, PrivateKeeperDetailsFormModel, SetupTradeDetailsFormModel}
import common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import common.webserviceclients.common.MicroserviceResponse
import views.acquire.VehicleLookup.VehicleSoldTo_Private
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid, addressWithoutUprn}
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.UprnValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.{ReferenceNumberValid, RegistrationNumberValid, TransactionIdValid}
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.{TransactionTimestampValid, VehicleMakeValid, VehicleModelValid}
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts

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

  def withIdentifier(id: String)(implicit webDriver: WebDriver) = {
    webDriver.manage().addCookie(new Cookie(IdentifierCacheKey, id))
    this
  }

  def setupTradeDetails(traderBusinessName: String = TraderBusinessNameValid,
                        traderPostcode: String = PostcodeValid,
                        traderEmail: Option[String] = None)(implicit webDriver: WebDriver) = {
    val key = setupTradeDetailsCacheKey
    val value = SetupTradeDetailsFormModel(
      traderBusinessName,
      traderPostcode = traderPostcode,
      traderEmail = traderEmail
    )
    addCookie(key, value)
    this
  }

  def businessChooseYourAddress(uprn: Long = UprnValid)(implicit webDriver: WebDriver) = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprn.toString)
    addCookie(key, value)
    this
  }

  def enterAddressManually(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                           line2: Option[String] = Some(Line2Valid),
                           line3: Option[String] = Some(Line3Valid),
                           postTown: String = PostTownValid,
                           postCode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyFormModel(addressAndPostcodeModel = AddressAndPostcodeViewModel(
      addressLinesModel = AddressLinesViewModel(
        buildingNameOrNumber = buildingNameOrNumber,
        line2 = line2,
        line3 = line3,
        postTown = postTown
      ),
      postCode = postCode)
    )
    addCookie(key, value)
    this
  }

  def dealerDetails(address: AddressModel = addressWithoutUprn)(implicit webDriver: WebDriver) = {
    val key = traderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = address,
      traderEmail = None
    )
    addCookie(key, value)
    this
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString)
                                   (implicit webDriver: WebDriver) = {
    val key = bruteForcePreventionViewModelCacheKey
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
                             vehicleSoldTo: String = VehicleSoldTo_Private)(implicit webDriver: WebDriver) = {
    val key = VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormModel(referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      vehicleSoldTo = vehicleSoldTo)
    addCookie(key, value)
    this
  }

  def vehicleLookupResponse(responseMessage: String = VehicleLookupFailureResponseMessage)
                           (implicit webDriver: WebDriver) = {
    val key = MsResponseCacheKey
    val value = MicroserviceResponseModel(MicroserviceResponse("", responseMessage))
    addCookie(key, value)
    this
  }

  def vehicleAndKeeperDetails(registrationNumber: String = RegistrationNumberValid,
                              vehicleMake: Option[String] = Some(VehicleMakeValid),
                              vehicleModel: Option[String] = Some(VehicleModelValid),
                              title: Option[String] = None,
                              firstName: Option[String] = None,
                              lastName: Option[String] = None,
                              address: Option[AddressModel] = None,
                              keeperEndDate: Option[DateTime] = None,
                              disposeFlag: Option[Boolean] = Some(false),
                              suppressedV5CFlag: Option[Boolean] = Some(false))(implicit webDriver: WebDriver) = {
    val key = vehicleAndKeeperLookupDetailsCacheKey
    val value = VehicleAndKeeperDetailsModel(
      registrationNumber = registrationNumber,
      make = vehicleMake,
      model = vehicleModel,
      title = title,
      firstName = firstName,
      lastName = lastName,
      address = address,
      keeperEndDate = keeperEndDate,
      keeperChangeDate = None,
      disposeFlag = disposeFlag,
      suppressedV5Flag = suppressedV5CFlag
    )
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
    val key = privateKeeperDetailsCacheKey
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
    val key = businessKeeperDetailsCacheKey
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
                       isBusinessKeeper: Boolean = false)(implicit webDriver: WebDriver) = {
    val key = newKeeperDetailsCacheKey
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
      displayName = if (businessName.isEmpty) firstName + " " + lastName
                    else businessName.getOrElse("")
    )
    addCookie(key, value)
    this
  }

  def newKeeperEnterAddressManually(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                           line2: Option[String] = Some(Line2Valid),
                           line3: Option[String] = Some(Line3Valid),
                           postTown: String = PostTownValid,
                           postCode: String = PostcodeValid)(implicit webDriver: WebDriver) = {
    val key = newKeeperEnterAddressManuallyCacheKey
    val value = EnterAddressManuallyFormModel(addressAndPostcodeModel = AddressAndPostcodeViewModel(
      addressLinesModel = AddressLinesViewModel(
        buildingNameOrNumber = buildingNameOrNumber,
        line2 = line2,
        line3 = line3,
        postTown = postTown
      ),
      postCode = postCode
    ))
    addCookie(key, value)
    this
  }

  def vehicleTaxOrSornFormModel(sornVehicle: Option[String] = None,
                                select: String = SornId)(implicit webDriver: WebDriver) = {
    val key = VehicleTaxOrSornCacheKey
    val value = VehicleTaxOrSornFormModel(sornVehicle = sornVehicle, select = select)
    addCookie(key, value)
    this
  }

  def preventGoingToCompleteAndConfirmPageCookie()(implicit webDriver: WebDriver) = {
    addCookie(CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey, "true")
  }

  def completeAndConfirmResponseModelModel(id: String = TransactionIdValid,
                                           timestamp: DateTime = TransactionTimestampValid)
                                          (implicit webDriver: WebDriver) = {
    val key = AcquireCompletionResponseCacheKey
    val value = CompleteAndConfirmResponseModel(id, timestamp)
    addCookie(key, value)
    this
  }

  def completeAndConfirmDetails(mileage: Option[Int] = Some(MileageValid.toInt),
                              dateOfSale: LocalDate = new LocalDate(
                                YearDateOfSaleValid.toInt,
                                MonthDateOfSaleValid.toInt,
                                DayDateOfSaleValid.toInt),
                              consent: String = ConsentTrue)(implicit webDriver: WebDriver) = {
    val key = CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
    val value = CompleteAndConfirmFormModel(
      mileage,
      dateOfSale,
      consent
    )
    addCookie(key, value)
    this
  }
}
