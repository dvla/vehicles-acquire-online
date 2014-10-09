package controllers

import com.google.inject.Inject
import models.NewKeeperDetailsViewModel
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
     request.cookies.getModel[VehicleDetailsModel]) match {
      case (Some(newKeeperDetails), Some(traderDetails), Some(vehicleDetails)) =>
        Ok(views.html.acquire.acquire_success())
      case _ => Ok(views.html.acquire.acquire_success())
    }
  }
}