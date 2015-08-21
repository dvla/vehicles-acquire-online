package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.{AllCacheKeys, VehicleLookupCacheKeys}
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.LogFormats.DVLALogger
import common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config

class KeeperStillOnRecord @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller with DVLALogger {

  def present = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Warn, "Keeper still on record")
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(vehicleAndKeeperDetails) =>
        Ok(views.html.acquire.keeper_still_on_record(vehicleAndKeeperDetails))
      case _ =>
        logMessage(request.cookies.trackingId(), Warn,
          "Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails.")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def buyAnotherVehicle = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Debug, s"Now redirecting to ${routes.VehicleLookup.present()}")
    Redirect(routes.VehicleLookup.present()).
      discardingCookies(VehicleLookupCacheKeys)
  }

  def finish = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Debug, s"Now redirecting to ${routes.BeforeYouStart.present()}")
    Redirect(routes.BeforeYouStart.present()).
      discardingCookies(AllCacheKeys)
  }
}