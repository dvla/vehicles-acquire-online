package helpers.acquire

import composition.TestComposition
import controllers.MicroServiceError.MicroServiceErrorRefererCacheKey
import org.joda.time.LocalDate
import pages.acquire.VehicleLookupPage
import play.api.i18n.Messages
import play.api.libs.json.{Json, Writes}
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.mappings.TitlePickerString
import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{BruteForcePreventionModel, VehicleDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, AddressModel}
import common.clientsidesession.{ClearTextClientSideSession, ClientSideSessionFactory, CookieFlags}
import common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import models._
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.SetupTradeDetailsFormModel.SetupTradeDetailsCacheKey
import models.VehicleLookupFormModel.{VehicleLookupFormModelCacheKey, VehicleLookupResponseCodeCacheKey}
import TraderDetailsModel.TraderDetailsCacheKey
import pages.acquire.SetupTradeDetailsPage.{TraderBusinessNameValid, PostcodeValid}
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.UprnValid
import webserviceclients.fakes.FakeVehicleLookupWebService.{ReferenceNumberValid, RegistrationNumberValid, VehicleMakeValid}
import webserviceclients.fakes.FakeAddressLookupService.{BuildingNameOrNumberValid, Line2Valid, Line3Valid, PostTownValid}
import pages.acquire.BusinessKeeperDetailsPage.{FleetNumberValid, BusinessNameValid, EmailValid}
import pages.acquire.PrivateKeeperDetailsPage.{ModelValid,  FirstNameValid, LastNameValid, DriverNumberValid}
import pages.acquire.CompleteAndConfirmPage.MileageValid
import pages.acquire.PrivateKeeperDetailsPage.{YearDateOfBirthValid, DayDateOfBirthValid, MonthDateOfBirthValid}
import pages.acquire.CompleteAndConfirmPage.{DayDateOfSaleValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import views.acquire.VehicleLookup.VehicleSoldTo_Private
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl.MaxAttempts
import play.api.mvc.Cookie

object CookieFactoryForUnitSpecs extends TestComposition { // TODO can we make this more fluent by returning "this" at the end of the defs

  implicit private val cookieFlags = injector.getInstance(classOf[CookieFlags])
  final val TrackingIdValue = "trackingId"
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
    val value = "yes" // TODO make a constant
    createCookie(key, value)
  }

  def setupTradeDetails(traderPostcode: String = PostcodeValid, traderEmail: Option[String] = None): Cookie = {
    val key = SetupTradeDetailsCacheKey
    val value = SetupTradeDetailsFormModel(traderBusinessName = TraderBusinessNameValid,
      traderPostcode = traderPostcode, traderEmail = traderEmail)
    createCookie(key, value)
  }

  def businessChooseYourAddress(): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressFormModel(uprnSelected = UprnValid.toString)
    createCookie(key, value)
  }

  def enterAddressManually(): Cookie = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyFormModel(
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
                         traderPostcode: String = PostcodeValid): Cookie = {
    val key = TraderDetailsCacheKey
    val value = TraderDetailsModel(
      traderName = TraderBusinessNameValid,
      traderAddress = AddressModel(
        uprn = uprn,
        address = Seq(buildingNameOrNumber, line2, line3, postTown, traderPostcode)
      )
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
                          vehicleModel: String = ModelValid,
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

  def vehicleLookupResponseCode(responseCode: String = "disposal_vehiclelookupfailure"): Cookie =
    createCookie(VehicleLookupResponseCodeCacheKey, responseCode)

  def privateKeeperDetailsModel(title: String = Messages(TitlePickerString.standardOptions(0)),
                                firstName: String = FirstNameValid,
                                lastName: String = LastNameValid,
                                dateOfBirth: Option[LocalDate] = Some(new LocalDate(
                                  YearDateOfBirthValid.toInt,
                                  MonthDateOfBirthValid.toInt,
                                  DayDateOfBirthValid.toInt)),
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
                              DayDateOfSaleValid.toInt)): Cookie = {
    val key = CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
    val value = CompleteAndConfirmFormModel(
      mileage,
      dateOfSale,
      ""
    )
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

  def newKeeperChooseYourAddress(): Cookie = {
    val key = NewKeeperChooseYourAddressFormModel.NewKeeperChooseYourAddressCacheKey
    val value = NewKeeperChooseYourAddressFormModel(uprnSelected = UprnValid.toString)
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
}