package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.AcquireCompletionViewModel
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmResponseModel
import models.VehicleTaxOrSornFormModel
import models.{AllCacheKeys, VehicleNewKeeperCompletionCacheKeys}
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.{NewKeeperDetailsViewModel, VehicleAndKeeperDetailsModel, TraderDetailsModel}
import utils.helpers.Config

class AcquireFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  private final val MissingCookiesAcquireFailure = "Missing cookies in cache. Acquire was failed, however cannot " +
    "display failure page. Redirecting to BeforeYouStart"
  private final val MissingCookies = "Missing cookies in cache."

  def present = Action { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[TraderDetailsModel],
      request.cookies.getModel[NewKeeperDetailsViewModel],
      request.cookies.getModel[CompleteAndConfirmFormModel],
      request.cookies.getModel[VehicleTaxOrSornFormModel],
      request.cookies.getModel[CompleteAndConfirmResponseModel]
      ) match {
      case (Some(vehiclAndKeepereDetailsModel), Some(traderDetailsModel), Some(newKeeperDetailsModel),
      Some(completeAndConfirmModel), Some(taxOrSornModel), Some(responseModel)) =>
        Ok(views.html.acquire.acquire_failure(AcquireCompletionViewModel(vehiclAndKeepereDetailsModel,
          traderDetailsModel, newKeeperDetailsModel, completeAndConfirmModel, taxOrSornModel, responseModel)))
      case _ => redirectToStart(MissingCookiesAcquireFailure)
    }
  }

  def buyAnother = Action { implicit request =>
    val result = for {
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
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