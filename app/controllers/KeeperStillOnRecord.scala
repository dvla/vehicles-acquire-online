package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.{AllCacheKeys, VehicleLookupCacheKeys}
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.LogFormats.logMessage
import common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config

class KeeperStillOnRecord @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    Logger.warn(logMessage("Keeper still on record", request.cookies.trackingId()))
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(vehicleAndKeeperDetails) =>
        Ok(views.html.acquire.keeper_still_on_record(vehicleAndKeeperDetails))
      case _ =>
        Logger.warn(logMessage("Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails.",
          request.cookies.trackingId()))
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def buyAnotherVehicle = Action { implicit request =>
    Logger.debug(logMessage(s"Now redirecting to ${routes.VehicleLookup.present()}", request.cookies.trackingId()))
    Redirect(routes.VehicleLookup.present()).
      discardingCookies(VehicleLookupCacheKeys)
  }

  def finish = Action { implicit request =>
    Logger.debug(logMessage(s"Now redirecting to ${routes.BeforeYouStart.present()}", request.cookies.trackingId()))
    Redirect(routes.BeforeYouStart.present()).
      discardingCookies(AllCacheKeys)
  }
}