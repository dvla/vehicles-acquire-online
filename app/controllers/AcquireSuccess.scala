package controllers

import com.google.inject.Inject
import models.{CompleteAndConfirmFormModel, AcquireSuccessViewModel, NewKeeperDetailsViewModel}
import play.api.mvc.{Action, Controller}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config

import uk.gov.dvla.vehicles.presentation.common.model.{VehicleDetailsModel, TraderDetailsModel}

final class AcquireSuccess @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[NewKeeperDetailsViewModel],
     request.cookies.getModel[TraderDetailsModel],
     request.cookies.getModel[VehicleDetailsModel],
     request.cookies.getModel[CompleteAndConfirmFormModel]) match {
      case (Some(newKeeperDetails), Some(traderDetails), Some(vehicleDetails), Some(completeAndConfirmDetails)) =>
        val acquireSuccessDetails = AcquireSuccessViewModel(
          vehicleDetails = vehicleDetails,
          traderDetails = traderDetails,
          newKeeperDetails = newKeeperDetails,
          completeAndConfirmDetails = completeAndConfirmDetails
        )
        Ok(views.html.acquire.acquire_success(acquireSuccessDetails))
      case _ => Ok(views.html.acquire.error(""))
    }
  }
}