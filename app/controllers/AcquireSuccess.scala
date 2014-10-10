package controllers

import com.google.inject.Inject
import models.{AcquireSuccessViewModel, CompleteAndConfirmFormModel, NewKeeperDetailsViewModel}
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.model.{TraderDetailsModel, VehicleDetailsModel}
import utils.helpers.Config

final class AcquireSuccess @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request => {
    (for {
      newKeeperDetails <- request.cookies.getModel[NewKeeperDetailsViewModel]
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
      vehicleDetails <- request.cookies.getModel[VehicleDetailsModel]
      completeAndConfirmDetails <- request.cookies.getModel[CompleteAndConfirmFormModel]
    } yield
      Ok(views.html.acquire.acquire_success(AcquireSuccessViewModel(
        vehicleDetails = vehicleDetails,
        traderDetails = traderDetails,
        newKeeperDetails = newKeeperDetails,
        completeAndConfirmDetails = completeAndConfirmDetails
      )))
    ) getOrElse {
      Logger.warn("missing cookies in cache. Acquire successful, however cannot display success page")
      Redirect(routes.BeforeYouStart.present())
    }
  }}
}