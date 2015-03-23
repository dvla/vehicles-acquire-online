package controllers

import com.google.inject.Inject
import models.{AllCacheKeys, VehicleLookupCacheKeys}
import play.api.mvc.{Action, Controller}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichResult
import models.AcquireCacheKeyPrefix.CookiePrefix
import common.model.VehicleAndKeeperDetailsModel
import common.clientsidesession.CookieImplicits.RichCookies
import utils.helpers.Config

class SuppressedV5C @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                         config: Config) extends Controller {

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(vehicleAndKeeperDetails) =>
        Ok(views.html.acquire.suppressedV5C())
      case _ =>
        Logger.warn("Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails.")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def buyAnotherVehicle = Action { implicit request =>
    Redirect(routes.VehicleLookup.present()).
      discardingCookies(VehicleLookupCacheKeys)
  }

  def finish = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).
      discardingCookies(AllCacheKeys)
  }
}