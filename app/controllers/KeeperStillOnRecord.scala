package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichResult
import common.model.VehicleDetailsModel
import common.clientsidesession.CookieImplicits.RichCookies
import common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import utils.helpers.Config
import viewmodels.VehicleLookupFormViewModel.VehicleLookupFormModelCacheKey
import viewmodels.AllCacheKeys

final class KeeperStillOnRecord @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleDetailsModel] match {
      case Some(vehicleDetails) =>
        Ok(views.html.acquire.keeper_still_on_record(vehicleDetails))
      case _ =>
        Logger.warn("Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails.")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def buyAnotherVehicle = Action { implicit request =>
    Redirect(routes.VehicleLookup.present()).
      discardingCookies(Set(VehicleLookupFormModelCacheKey, VehicleLookupDetailsCacheKey))
  }

  def finish = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).
      discardingCookies(AllCacheKeys)
  }
}
