package controllers

import com.google.inject.Inject
import models.VehicleNewKeeperCompletionCacheKeys
import models.AcquireCompletionViewModel
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmResponseModel
import models.NewKeeperDetailsViewModel
import models.VehicleTaxOrSornFormModel
import models.AllCacheKeys
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.{TraderDetailsModel, VehicleDetailsModel}
import utils.helpers.Config

class AcquireSuccess @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  private final val MissingCookiesAcquireSuccess = "Missing cookies in cache. Acquire was successful, however cannot " +
    "display success page. Redirecting to BeforeYouStart"
  private final val MissingCookies = "Missing cookies in cache. Redirecting to BeforeYouStart"

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
        Ok(views.html.acquire.acquire_success(AcquireCompletionViewModel(vehicleDetailsModel,
          traderDetailsModel, newKeeperDetailsModel, completeAndConfirmModel, taxOrSornModel, responseModel)))
      case _ => redirectToStart(MissingCookiesAcquireSuccess)
    }
  }

  def buyAnother = Action { implicit request =>
    val result = for {
      acquireCompletionViewModel <- request.cookies.getModel[TraderDetailsModel]
    } yield Redirect(routes.VehicleLookup.present())
      .discardingCookies(VehicleNewKeeperCompletionCacheKeys)
    result getOrElse redirectToStart(MissingCookies)
  }

  def finish = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(AllCacheKeys)
  }

  private def redirectToStart(message: String) = {
    Logger.warn(message)
    Redirect(routes.BeforeYouStart.present())
  }
}
