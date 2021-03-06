package helpers.acquire

import composition.TestComposition
import controllers.MicroServiceError.MicroServiceErrorRefererCacheKey
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.BusinessChooseYourAddressFormModel
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmResponseModel
import models.EnterAddressManuallyFormModel
import models.{SurveyRequestTriggerDateCacheKey, VehicleLookupFormModel, VehicleTaxOrSornFormModel}
import models.VehicleTaxOrSornFormModel.Form.SornId
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import org.joda.time.{DateTime, LocalDate}
import pages.acquire.BusinessKeeperDetailsPage.{BusinessNameValid, EmailValid, FleetNumberValid}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MileageValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import pages.acquire.PrivateKeeperDetailsPage.DayDateOfBirthValid
import pages.acquire.PrivateKeeperDetailsPage.DriverNumberValid
import pages.acquire.PrivateKeeperDetailsPage.FirstNameValid
import pages.acquire.PrivateKeeperDetailsPage.LastNameValid
import pages.acquire.PrivateKeeperDetailsPage.MonthDateOfBirthValid
import pages.acquire.PrivateKeeperDetailsPage.YearDateOfBirthValid
import pages.acquire.SetupTradeDetailsPage.{PostcodeValid, TraderBusinessNameValid}
import pages.acquire.{BusinessChooseYourAddressPage, VehicleLookupPage}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.{ClearTextClientSideSession, ClientSideSessionFactory, CookieFlags, TrackingId}
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
import common.model.NewKeeperChooseYourAddressFormModel
import common.model.NewKeeperDetailsViewModel
import common.model.NewKeeperEnterAddressManuallyFormModel
import common.model.PrivateKeeperDetailsFormModel
import common.model.SetupTradeDetailsFormModel
import common.model.TraderDetailsModel
import common.model.VehicleAndKeeperDetailsModel
import common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey
import common.model.SeenCookieMessageCacheKey
import common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import common.webserviceclients.common.MicroserviceResponse
import views.acquire.VehicleLookup.VehicleSoldTo_Private
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.selectedAddress
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.ReferenceNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.RegistrationNumberValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.TransactionIdValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.TransactionTimestampValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.VehicleMakeValid
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.VehicleModelValid
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts

object CookieFactoryForUnitSpecs extends TestComposition {

  implicit private val cookieFlags = injector.getInstance(classOf[CookieFlags])
  final val TrackingIdValue = "trackingId"
  final val KeeperEmail = "abc@def.com"
  final val SeenCookieTrue = "yes"
  final val ConsentTrue = "true"
  final val VehicleLookupFailureResponseMessage = "disposal_vehiclelookupfailure"
  private val session = new ClearTextClientSideSession(TrackingId(TrackingIdValue))

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
    val key = setupTradeDetailsCacheKey
    val value = SetupTradeDetailsFormModel(
      traderBusinessName = traderBusinessName,
      traderPostcode = traderPostcode,
      traderEmail = traderEmail
    )
    createCookie(key, value)
  }

  def businessChooseYourAddressUseAddress(addressSelected: String = selectedAddress): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(addressSelected = addressSelected)
    createCookie(key, value)
  }

  def businessChooseYourAddress(addressSelected: String = BusinessChooseYourAddressPage.selectedAddress): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(addressSelected = addressSelected)
    createCookie(key, value)
  }

  def enterAddressManually(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                           line2: Option[String] = Some(Line2Valid),
                           line3: Option[String] = Some(Line3Valid),
                           postTown: String = PostTownValid,
                           postCode: String = PostcodeValid): Cookie = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyFormModel(
      addressAndPostcodeModel = AddressAndPostcodeViewModel(
        addressLinesModel = AddressLinesViewModel(
          buildingNameOrNumber = buildingNameOrNumber,
          line2 = line2,
          line3 = line3,
          postTown = postTown
        ),
        postCode = postCode
      )
    )
    createCookie(key, value)
  }

  def newKeeperEnterAddressManually(): Cookie = {
    val key = newKeeperEnterAddressManuallyCacheKey
    val value = NewKeeperEnterAddressManuallyFormModel(
      addressAndPostcodeModel = AddressAndPostcodeViewModel(
        addressLinesModel = AddressLinesViewModel(
          buildingNameOrNumber = BuildingNameOrNumberValid,
          line2 = Some(Line2Valid),
          line3 = Some(Line3Valid),
          postTown = PostTownValid
        ),
          postCode = PostcodeValid
      )
    )
    createCookie(key, value)
  }

  def traderDetailsModel(buildingNameOrNumber: String = BuildingNameOrNumberValid,
                         line2: String = Line2Valid,
                         line3: String = Line3Valid,
                         postTown: String = PostTownValid,
                         traderPostcode: String = PostcodeValid,
                         traderEmail: Option[String] = Some(EmailValid)): Cookie = {
    val key = traderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressModel(
        address = Seq(buildingNameOrNumber, line2, line3, postTown, traderPostcode)
      ),
      traderEmail = traderEmail
    )
    createCookie(key, value)
  }

  def bruteForcePreventionViewModel(permitted: Boolean = true,
                                    attempts: Int = 0,
                                    maxAttempts: Int = MaxAttempts,
                                    dateTimeISOChronology: String = org.joda.time.DateTime.now().toString): Cookie = {
    val key = bruteForcePreventionViewModelCacheKey
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
                                   disposeFlag: Option[Boolean] = Some(false),
                                   suppressedV5CFlag: Option[Boolean] = Some(false)): Cookie = {
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
    createCookie(key, value)
  }

  def vehicleLookupResponse(responseMessage: String = VehicleLookupFailureResponseMessage): Cookie =
    createCookie(MsResponseCacheKey, MicroserviceResponseModel(MicroserviceResponse("", responseMessage)))

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

  def newKeeperChooseYourAddress(addressSelected: String): Cookie = {
    val key = NewKeeperChooseYourAddressFormModel.newKeeperChooseYourAddressCacheKey
    val value = NewKeeperChooseYourAddressFormModel(addressSelected)
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
      address = AddressModel(address = Seq(buildingNameOrNumber, line2, line3, postTown, postcode)),
      email = email,
      isBusinessKeeper = isBusinessKeeper,
      displayName = if (businessName.isEmpty) firstName + " " + lastName
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
    val value = VehicleTaxOrSornFormModel(sornVehicle = sornVehicle, select = SornId)
    createCookie(key, value)
  }

  def allowGoingToCompleteAndConfirm(): Cookie =
    createCookie(CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey, "")

  def surveyUrl(surveyUrl: String): Cookie =
    createCookie(SurveyRequestTriggerDateCacheKey, surveyUrl)
}
