package helpers.acquire

import composition.TestComposition
import controllers.MicroServiceError.MicroServiceErrorRefererCacheKey
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey
import models.CompleteAndConfirmResponseModel
import models.EnterAddressManuallyFormModel
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.HelpCacheKey
import models.NewKeeperEnterAddressManuallyFormModel
import models.NewKeeperEnterAddressManuallyFormModel.NewKeeperEnterAddressManuallyCacheKey
import models.SeenCookieMessageCacheKey
import models.SetupTradeDetailsFormModel
import models.SetupTradeDetailsFormModel.SetupTradeDetailsCacheKey
import models.VehicleLookupFormModel
import models.VehicleLookupFormModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}
import models.VehicleTaxOrSornFormModel
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import org.joda.time.{DateTime, LocalDate}
import pages.acquire.{HelpPage, VehicleLookupPage}
import pages.acquire.SetupTradeDetailsPage.{TraderBusinessNameValid, PostcodeValid}
import play.api.mvc.Cookie
import pages.acquire.BusinessKeeperDetailsPage.{FleetNumberValid, BusinessNameValid, EmailValid}
import pages.acquire.PrivateKeeperDetailsPage.{FirstNameValid, LastNameValid, DriverNumberValid}
import pages.acquire.CompleteAndConfirmPage.MileageValid
import pages.acquire.PrivateKeeperDetailsPage.{YearDateOfBirthValid, DayDateOfBirthValid, MonthDateOfBirthValid}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import play.api.libs.json.{Json, Writes}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.{ClearTextClientSideSession, ClientSideSessionFactory, CookieFlags}
import common.mappings.TitleType
import common.model.AddressModel
import common.model.BruteForcePreventionModel
import common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import common.model.BusinessKeeperDetailsFormModel
import common.model.BusinessKeeperDetailsFormModel.businessKeeperDetailsCacheKey
import common.model.NewKeeperChooseYourAddressFormModel
import common.model.NewKeeperDetailsViewModel
import common.model.NewKeeperDetailsViewModel.newKeeperDetailsCacheKey
import common.model.PrivateKeeperDetailsFormModel
import common.model.PrivateKeeperDetailsFormModel.privateKeeperDetailsCacheKey
import common.model.TraderDetailsModel
import common.model.TraderDetailsModel.TraderDetailsCacheKey
import common.model.VehicleAndKeeperDetailsModel
import common.model.VehicleAndKeeperDetailsModel.VehicleAndKeeperLookupDetailsCacheKey
import common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import views.acquire.VehicleLookup.VehicleSoldTo_Private
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.UprnValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.VehicleModelValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.TransactionTimestampValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.ReferenceNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.RegistrationNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.VehicleMakeValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.TransactionIdValid
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}

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

  def vehicleAndKeeperDetailsModel(registrationNumber: String = RegistrationNumberValid,
                                   vehicleMake: Option[String] = Some(VehicleMakeValid),
                                   vehicleModel: Option[String] = Some(VehicleModelValid),
                                   title: Option[String] = None,
                                   firstName: Option[String] = None,
                                   lastName: Option[String] = None,
                                   address: Option[AddressModel] = None,
                                   keeperEndDate: Option[DateTime] = None,
                                   disposeFlag: Option[Boolean] = Some(false)): Cookie = {
    val key = VehicleAndKeeperLookupDetailsCacheKey
    val value = VehicleAndKeeperDetailsModel(
      registrationNumber = registrationNumber,
      make = vehicleMake,
      model = vehicleModel,
      title = title,
      firstName = firstName,
      lastName = lastName,
      address = address,
      keeperEndDate = keeperEndDate,
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
    val key = businessKeeperDetailsCacheKey
    val value = BusinessKeeperDetailsFormModel(
      fleetNumber = fleetNumber,
      businessName = businessName,
      email = email,
      postcode = postcode
    )
    createCookie(key, value)
  }

  def newKeeperChooseYourAddressUseUprn(uprnSelected: String = UprnValid.toString): Cookie = {
    val key = NewKeeperChooseYourAddressFormModel.newKeeperChooseYourAddressCacheKey
    val value = NewKeeperChooseYourAddressFormModel(uprnSelected = uprnSelected)
    createCookie(key, value)
  }

  def newKeeperChooseYourAddress(uprnSelected: String = "0"): Cookie = {
    val key = NewKeeperChooseYourAddressFormModel.newKeeperChooseYourAddressCacheKey
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
    val key = newKeeperDetailsCacheKey
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
