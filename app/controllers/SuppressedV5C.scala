package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.{AllCacheKeys, VehicleLookupCacheKeys}
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.VehicleAndKeeperDetailsModel
import common.LogFormats.logMessage
import utils.helpers.Config

class SuppressedV5C @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                         config: Config) extends Controller {

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(vehicleAndKeeperDetails) =>
        Ok(views.html.acquire.suppressedV5C())
      case _ =>
        Logger.warn(logMessage(s"Did not find VehicleDetailsModel cookie. " +
          s"Now redirecting to ${routes.SetUpTradeDetails.present()}",
          request.cookies.trackingId()))
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def buyAnotherVehicle = Action { implicit request =>
    Logger.debug(logMessage(s"Redirecting to ${routes.VehicleLookup.present()}", request.cookies.trackingId()))
    Redirect(routes.VehicleLookup.present()).
      discardingCookies(VehicleLookupCacheKeys)
  }

  def finish = Action { implicit request =>
    Logger.debug(logMessage(s"Redirecting to ${routes.BeforeYouStart.present()}", request.cookies.trackingId()))
    Redirect(routes.BeforeYouStart.present()).
      discardingCookies(AllCacheKeys)
  }
}