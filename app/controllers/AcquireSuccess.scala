package controllers

import com.google.inject.Inject
import models.{AcquireSuccessViewModel, CompleteAndConfirmFormModel, NewKeeperDetailsViewModel}
import NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{VehicleDetailsModel, TraderDetailsModel}
import VehicleDetailsModel.VehicleLookupDetailsCacheKey
import CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.{TraderDetailsModel, VehicleDetailsModel}
import utils.helpers.Config

final class AcquireSuccess @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    val result = for {
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
    result getOrElse {
      Logger.warn("missing cookies in cache. Acquire successful, however cannot display success page")
      Redirect(routes.BeforeYouStart.present())
    }
  }

  def buyAnother = Action { implicit request =>
    val result = for {
      newKeeperDetails <- request.cookies.getModel[NewKeeperDetailsViewModel]
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
      vehicleDetails <- request.cookies.getModel[VehicleDetailsModel]
      completeAndConfirmDetails <- request.cookies.getModel[CompleteAndConfirmFormModel]
    } yield Redirect(routes.VehicleLookup.present())
        .discardingCookies(Set(NewKeeperDetailsCacheKey, VehicleLookupDetailsCacheKey, CompleteAndConfirmCacheKey))
    result getOrElse {
      Logger.warn("missing cookies in cache.")
      Redirect(routes.BeforeYouStart.present())
    }
  }
}