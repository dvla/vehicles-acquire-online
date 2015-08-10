package controllers

import javax.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import play.api.Logger
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.controllers.NewKeeperChooseYourAddressBase
import common.model.NewKeeperChooseYourAddressViewModel
import common.webserviceclients.addresslookup.AddressLookupService
import common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.Config
import views.html.acquire.new_keeper_choose_your_address

class NewKeeperChooseYourAddress @Inject()(protected override val addressLookupService: AddressLookupService)
                                          (implicit protected override val clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config) extends NewKeeperChooseYourAddressBase(addressLookupService) {

  override protected def ordnanceSurveyUseUprn: Boolean = config.ordnanceSurveyUseUprn

  override protected def invalidFormResult(model: NewKeeperChooseYourAddressViewModel,
                                           name: String,
                                           postcode: String,
                                           email: Option[String],
                                           addresses: Seq[(String, String)],
                                           isBusinessKeeper: Boolean = false,
                                           fleetNumber: Option[String] = None)(implicit request: Request[_]): Result =
    BadRequest(new_keeper_choose_your_address(
      model,
      name,
      postcode,
      email,
      addresses
    ))

  override protected def presentView(model: NewKeeperChooseYourAddressViewModel,
                                     name: String,
                                     postcode: String,
                                     email: Option[String],
                                     addresses: Seq[(String, String)],
                                     isBusinessKeeper: Boolean = false,
                                     fleetNumber: Option[String] = None)(implicit request: Request[_]): Result =
    Ok(views.html.acquire.new_keeper_choose_your_address(
      model, name, postcode, email, addresses)
    )

  override protected def privateKeeperDetailsRedirect(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(),Debug,s"Redirecting to ${routes.PrivateKeeperDetails.present()}")
    Redirect(routes.PrivateKeeperDetails.present())
  }

  override protected def businessKeeperDetailsRedirect(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(),Debug,s"Redirecting to ${routes.BusinessKeeperDetails.present()}")
    Redirect(routes.BusinessKeeperDetails.present())
  }

  override protected def vehicleLookupRedirect(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(),Debug,s"Redirecting to ${routes.VehicleLookup.present()}")
    Redirect(routes.VehicleLookup.present())
  }

  override protected def completeAndConfirmRedirect(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(),Debug,s"Redirecting to ${routes.VehicleTaxOrSorn.present()}")
    Redirect(routes.VehicleTaxOrSorn.present())
  }

  override protected def upnpNotFoundRedirect(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(),Debug,s"Redirecting to ${routes.UprnNotFound.present()}")
    Redirect(routes.UprnNotFound.present())
  }
}
