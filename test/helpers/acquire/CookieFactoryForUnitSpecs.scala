package helpers.acquire

import composition.TestComposition
import controllers.MicroServiceError.MicroServiceErrorRefererCacheKey
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey
import org.joda.time.{DateTime, LocalDate}
import pages.acquire.{HelpPage, VehicleLookupPage}
import play.api.libs.json.{Json, Writes}
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.mappings.TitleType
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{BruteForcePreventionModel, VehicleDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, AddressModel}
import common.clientsidesession.{ClearTextClientSideSession, ClientSideSessionFactory, CookieFlags}
import uk.gov.dvla.vehicles.presentation.common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import models.{CompleteAndConfirmResponseModel, SeenCookieMessageCacheKey, SetupTradeDetailsFormModel, BusinessChooseYourAddressFormModel}
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.BusinessKeeperDetailsFormModel
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.CompleteAndConfirmFormModel
import models.EnterAddressManuallyFormModel
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.NewKeeperChooseYourAddressFormModel
import models.NewKeeperDetailsViewModel
import models.NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import models.NewKeeperEnterAddressManuallyFormModel
import models.NewKeeperEnterAddressManuallyFormModel.NewKeeperEnterAddressManuallyCacheKey
import models.PrivateKeeperDetailsFormModel
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.SetupTradeDetailsFormModel.SetupTradeDetailsCacheKey
import models.VehicleLookupFormModel
import models.VehicleLookupFormModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}
import models.VehicleTaxOrSornFormModel
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import TraderDetailsModel.TraderDetailsCacheKey
import pages.acquire.SetupTradeDetailsPage.{TraderBusinessNameValid, PostcodeValid}
import play.api.mvc.Cookie
import pages.acquire.BusinessKeeperDetailsPage.{FleetNumberValid, BusinessNameValid, EmailValid}
import pages.acquire.PrivateKeeperDetailsPage.{FirstNameValid, LastNameValid, DriverNumberValid}
import pages.acquire.CompleteAndConfirmPage.MileageValid
import pages.acquire.PrivateKeeperDetailsPage.{YearDateOfBirthValid, DayDateOfBirthValid, MonthDateOfBirthValid}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import views.acquire.VehicleLookup.VehicleSoldTo_Private
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.UprnValid
import webserviceclients.fakes.FakeVehicleLookupWebService.VehicleModelValid
import webserviceclients.fakes.FakeVehicleLookupWebService.TransactionTimestampValid
import webserviceclients.fakes.FakeVehicleLookupWebService.ReferenceNumberValid
import webserviceclients.fakes.FakeVehicleLookupWebService.RegistrationNumberValid
import webserviceclients.fakes.FakeVehicleLookupWebService.VehicleMakeValid
import webserviceclients.fakes.FakeVehicleLookupWebService.TransactionIdValid
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}
import models.HelpCacheKey

object CookieFactoryForUnitSpecs extends TestComposition {

  implicit private val cookieFlags = injector.getInstance(classOf[CookieFlags])
  final val TrackingIdValue = "trackingId"
  final val KeeperEmail = "abc@def.com"
  final val SeenCookieTrue = "yes"
  final val ConsentTrue = "true"
  final val VehicleLookupFailureResponseCode = "disposal_vehiclelookupfailure"
  private val session = new ClearTextClientSideSession(TrackingIdValue)

  private def createCookie[A](key: String, value: A)(implicit tjs: Writes[A]): Cookie = {
    val json = Json.toJson(value).toString()
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, json)
  }

  private def createCookie[A](key: String, value: String): Cookie = {
    val cookieName = session.nameCookie(key)
    session.newCookie(cookieName, value)
  }

  def seenCookieMessage(): Cookie = {
    val key = SeenCookieMessageCacheKey
    val value = SeenCookieTrue
    createCookie(key, value)
  }

  def setupTradeDetails(traderBusinessName: String = TraderBusinessNameValid,
                        traderPostcode: String = PostcodeValid,
                        traderEmail: Option[String] = None): Cookie = {
    val key = SetupTradeDetailsCacheKey
    val value = SetupTradeDetailsFormModel(
      traderBusinessName = traderBusinessName,
      traderPostcode = traderPostcode,
      traderEmail = traderEmail
    )
    createCookie(key, value)
  }

  def businessChooseYourAddressUseUprn(uprnSelected: String = UprnValid.toString): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }

  def businessChooseYourAddress(uprnSelected: String = "0"): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }

  def enterAddressManually(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                           line2: Option[String] = Some(Line2Valid),
                           line3: Option[String] = Some(Line3Valid),
                           postTown: String = PostTownValid): Cookie = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyFormModel(
      addressAndPostcodeModel = AddressAndPostcodeViewModel(
        addressLinesModel = AddressLinesViewModel(
          buildingNameOrNumber = buildingNameOrNumber,
          line2 = line2,
          line3 = line3,
          postTown = postTown
        )
      )
    )
    createCookie(key, value)
  }

  def newKeeperEnterAddressManually(): Cookie = {
    val key = NewKeeperEnterAddressManuallyCacheKey
    val value = NewKeeperEnterAddressManuallyFormModel(
      addressAndPostcodeModel = AddressAndPostcodeViewModel(
        addressLinesModel = AddressLinesViewModel(
          buildingNameOrNumber = BuildingNameOrNumberValid,
          line2 = Some(Line2Valid),
          line3 = Some(Line3Valid),
          postTown = PostTownValid
        )
      )
    )
    createCookie(key, value)
  }

  def traderDetailsModel(uprn: Option[Long] = None,
                         buildingNameOrNumber: String = BuildingNameOrNumberValid,
                         line2: String = Line2Valid,
                         line3: String = Line3Valid,
                         postTown: String = PostTownValid,
                         traderPostcode: String = PostcodeValid,
                         traderEmail: String = EmailValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressModel(
        uprn = uprn,
        address = Seq(buildingNameOrNumber, line2, line3, postTown, traderPostcode)
      ),
      traderEmail = Some(traderEmail)
    )
    createCookie(key, value)
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString): Cookie = {
    val key = BruteForcePreventionViewModelCacheKey
    val value = BruteForcePreventionModel(
      permitted,
      attempts,
      maxAttempts,
      dateTimeISOChronology = dateTimeISOChronology
    )
    createCookie(key, value)
  }

  def vehicleLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                             registrationNumber: String = RegistrationNumberValid,
                             vehicleSoldTo: String = VehicleSoldTo_Private): Cookie = {
    val key = VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormModel(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      vehicleSoldTo = vehicleSoldTo
    )
    createCookie(key, value)
  }

  def vehicleDetailsModel(registrationNumber: String = RegistrationNumberValid,
                          vehicleMake: String = VehicleMakeValid,
                          vehicleModel: String = VehicleModelValid,
                          disposeFlag: Boolean = false): Cookie = {
    val key = VehicleLookupDetailsCacheKey
    val value = VehicleDetailsModel(
      registrationNumber = registrationNumber,
      vehicleMake = vehicleMake,
      vehicleModel = vehicleModel,
      disposeFlag = disposeFlag
    )
    createCookie(key, value)
  }

  def vehicleLookupResponseCode(responseCode: String = VehicleLookupFailureResponseCode): Cookie =
    createCookie(VehicleLookupResponseCodeCacheKey, responseCode)

  def privateKeeperDetailsModel(title: TitleType = TitleType(1, ""),
                                firstName: String = FirstNameValid,
                                lastName: String = LastNameValid,
                                dateOfBirth: Option[LocalDate] = Some(
                                  new LocalDate(
                                    YearDateOfBirthValid.toInt,
                                    MonthDateOfBirthValid.toInt,
                                    DayDateOfBirthValid.toInt
                                  )
                                ),
                                email: Option[String] = Some(EmailValid),
                                driverNumber: Option[String] = Some(DriverNumberValid),
                                postcode: String = PostcodeValid): Cookie = {
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
    createCookie(key, value)
  }

  def completeAndConfirmModel(mileage: Option[Int] = Some(MileageValid.toInt),
                              dateOfSale: LocalDate = new LocalDate(
                                YearDateOfSaleValid.toInt,
                                MonthDateOfSaleValid.toInt,
                                DayDateOfSaleValid.toInt),
                              consent: String = ConsentTrue): Cookie = {
    val key = CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
    val value = CompleteAndConfirmFormModel(
      mileage,
      dateOfSale,
      consent
    )
    createCookie(key, value)
  }

  def completeAndConfirmResponseModelModel(id: String = TransactionIdValid,
                                           timestamp: DateTime = TransactionTimestampValid): Cookie = {
    val key = AcquireCompletionResponseCacheKey
    val value = CompleteAndConfirmResponseModel(id, timestamp)
    createCookie(key, value)
  }

  def businessKeeperDetailsModel(fleetNumber: Option[String] = Some(FleetNumberValid),
                                businessName: String = BusinessNameValid,
                                email: Option[String] = Some(EmailValid),
                                postcode: String = PostcodeValid) : Cookie = {
    val key = BusinessKeeperDetailsCacheKey
    val value = BusinessKeeperDetailsFormModel(
      fleetNumber = fleetNumber,
      businessName = businessName,
      email = email,
      postcode = postcode
    )
    createCookie(key, value)
  }

  def newKeeperChooseYourAddressUseUprn(uprnSelected: String = UprnValid.toString): Cookie = {
    val key = NewKeeperChooseYourAddressFormModel.NewKeeperChooseYourAddressCacheKey
    val value = NewKeeperChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }

  def newKeeperChooseYourAddress(uprnSelected: String = "0"): Cookie = {
    val key = NewKeeperChooseYourAddressFormModel.NewKeeperChooseYourAddressCacheKey
    val value = NewKeeperChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }
  def newKeeperDetailsModel(title: Option[TitleType] = None,
                            firstName: Option[String] = None,
                            lastName: Option[String] = None,
                            dateOfBirth: Option[LocalDate] = None,
                            driverNumber: Option[String] = None,
                            businessName: Option[String] = None,
                            fleetNumber: Option[String] = None,
                            email: Option[String] = None,
                            isBusinessKeeper: Boolean = false,
                            uprn: Option[Long] = None,
                            buildingNameOrNumber: String = BuildingNameOrNumberValid,
                            line2: String = Line2Valid,
                            line3: String = Line3Valid,
                            postTown: String = PostTownValid,
                            postcode: String = PostcodeValid): Cookie = {
    val key = NewKeeperDetailsCacheKey
    val value = NewKeeperDetailsViewModel(
      title = title,
      firstName = firstName,
      lastName = lastName,
      dateOfBirth = dateOfBirth,
      driverNumber = driverNumber,
      businessName = businessName,
      fleetNumber = fleetNumber,
      address = AddressModel(uprn = uprn, address = Seq(buildingNameOrNumber, line2, line3, postTown, postcode)),
      email = email,
      isBusinessKeeper = isBusinessKeeper,
      displayName = if (businessName == None) firstName + " " + lastName
                    else businessName.getOrElse("")
    )
    createCookie(key, value)
  }

  def trackingIdModel(value: String = TrackingIdValue): Cookie = {
    createCookie(ClientSideSessionFactory.TrackingIdCookieName, value)
  }

  def microServiceError(origin: String = VehicleLookupPage.address): Cookie = {
    val key = MicroServiceErrorRefererCacheKey
    val value = origin
    createCookie(key, value)
  }

  def vehicleTaxOrSornFormModel(sornVehicle: Option[String] = None): Cookie = {
    val key = VehicleTaxOrSornCacheKey
    val value = VehicleTaxOrSornFormModel(sornVehicle = sornVehicle)
    createCookie(key, value)
  }

  def help(origin: String = HelpPage.address): Cookie = {
    val key = HelpCacheKey
    val value = origin
    createCookie(key, value)
  }

  def allowGoingToCompleteAndConfirm(): Cookie =
    createCookie(CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey, "")
}
