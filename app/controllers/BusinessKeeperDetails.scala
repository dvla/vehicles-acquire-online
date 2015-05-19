package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import play.api.Logger
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.controllers.BusinessKeeperDetailsBase
import common.model.BusinessKeeperDetailsViewModel
import uk.gov.dvla.vehicles.presentation.common.LogFormats.logMessage
import utils.helpers.Config

class BusinessKeeperDetails @Inject()()(implicit protected override val clientSideSessionFactory: ClientSideSessionFactory,
                                            val config: Config) extends BusinessKeeperDetailsBase {

  protected override def presentResult(model: BusinessKeeperDetailsViewModel)(implicit request: Request[_]): Result =
    Ok(views.html.acquire.business_keeper_details(model))

  protected def invalidFormResult(model:BusinessKeeperDetailsViewModel)(implicit request: Request[_]): Result =
    BadRequest(views.html.acquire.business_keeper_details(model))

  protected def missingVehicleDetails(implicit request: Request[_]): Result = {
    Logger.warn(logMessage(s"Missing vehicle details, now redirecting to ${routes.VehicleLookup.present()}", request.cookies.trackingId()))
    Redirect(routes.SetUpTradeDetails.present())
  }

  protected def success(implicit request: Request[_]): Result = {
    Logger.debug(logMessage(s"Redirecting to ${routes.NewKeeperChooseYourAddress.present()}", request.cookies.trackingId()))
    Redirect(routes.NewKeeperChooseYourAddress.present())
  }
}
