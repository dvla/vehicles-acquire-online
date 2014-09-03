package helpers.acquire

import composition.TestComposition
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Cookie
import uk.gov.dvla.vehicles.presentation.common
import common.model.{TraderDetailsModel, AddressModel}
import common.clientsidesession.{ClearTextClientSideSession, ClientSideSessionFactory, CookieFlags}
import common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import viewmodels._
import viewmodels.SetupTradeDetailsViewModel.SetupTradeDetailsCacheKey
import viewmodels.BusinessChooseYourAddressViewModel.BusinessChooseYourAddressCacheKey
import viewmodels.EnterAddressManuallyViewModel.EnterAddressManuallyCacheKey
import viewmodels.VehicleLookupFormViewModel.VehicleLookupFormModelCacheKey
import TraderDetailsModel.TraderDetailsCacheKey
import pages.acquire.SetupTradeDetailsPage.{TraderBusinessNameValid, PostcodeValid}
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl._
import webserviceclients.fakes.FakeVehicleLookupWebService._
import webserviceclients.fakes.FakeAddressLookupService._
import views.acquire.VehicleLookup.VehicleSoldTo_Private

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
    val value = SetupTradeDetailsViewModel(traderBusinessName = TraderBusinessNameValid,
      traderPostcode = traderPostcode, traderEmail = traderEmail)
    createCookie(key, value)
  }

  def businessChooseYourAddress(): Cookie = {
    val key = BusinessChooseYourAddressCacheKey
    val value = BusinessChooseYourAddressViewModel(uprnSelected = traderUprnValid.toString)
    createCookie(key, value)
  }

  def enterAddressManually(): Cookie = {
    val key = EnterAddressManuallyCacheKey
    val value = EnterAddressManuallyViewModel(
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

  def vehicleLookupFormModel(referenceNumber: String = ReferenceNumberValid,
                             registrationNumber: String = RegistrationNumberValid,
                             vehicleSoldTo: String = VehicleSoldTo_Private): Cookie = {
    val key = VehicleLookupFormModelCacheKey
    val value = VehicleLookupFormViewModel(
      referenceNumber = referenceNumber,
      registrationNumber = registrationNumber,
      vehicleSoldTo = vehicleSoldTo
    )
    createCookie(key, value)
  }

  def trackingIdModel(value: String = TrackingIdValue): Cookie = {
    createCookie(ClientSideSessionFactory.TrackingIdCookieName, value)
  }
}
