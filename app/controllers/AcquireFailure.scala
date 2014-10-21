package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import play.api.Logger
import models.{AllCacheKeys, VehicleNewKeeperCompletionCacheKeys}
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.{VehicleDetailsModel, TraderDetailsModel}
import utils.helpers.Config
import models.{VehicleTaxOrSornFormModel, CompleteAndConfirmResponseModel, CompleteAndConfirmFormModel, NewKeeperDetailsViewModel, AcquireCompletionViewModel}

final class AcquireFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getModel[VehicleDetailsModel],
      request.cookies.getModel[TraderDetailsModel],
      request.cookies.getModel[NewKeeperDetailsViewModel],
      request.cookies.getModel[CompleteAndConfirmFormModel],
      request.cookies.getModel[VehicleTaxOrSornFormModel],
      request.cookies.getModel[CompleteAndConfirmResponseModel]
      ) match {
      case (Some(vehicleDetailsModel), Some(traderDetailsModel), Some(newKeeperDetailsModel),
      Some(completeAndConfirmModel), Some(taxOrSornModel), Some(responseModel)) =>
        Ok(views.html.acquire.acquire_failure(AcquireCompletionViewModel(vehicleDetailsModel,
          traderDetailsModel, newKeeperDetailsModel, completeAndConfirmModel, taxOrSornModel, responseModel)))
      case _ => {
        Logger.warn("Missing cookies in cache. Acquire was failed, however cannot display failure page. " +
          "Redirecting to BeforeYouStart")
        Redirect(routes.BeforeYouStart.present())
      }
    }
  }

  def buyAnother = Action { implicit request =>
    val result = for {
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
    } yield Redirect(routes.VehicleLookup.present())
        .discardingCookies(VehicleNewKeeperCompletionCacheKeys)
    result getOrElse {
      Logger.warn("missing cookies in cache.")
      Redirect(routes.BeforeYouStart.present())
    }
  }

  def finish = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
        .discardingCookies(AllCacheKeys)
  }
}