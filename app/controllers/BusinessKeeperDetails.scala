package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies

final class BusinessKeeperDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleDetailsModel] match {
      case Some(vehicleDetails) =>
        Ok(views.html.acquire.business_keeper_details(vehicleDetails))
      case _ =>
        Logger.warn("Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails.")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }
}
