package controllers

import com.google.inject.Inject
import models.AcquireCompletionViewModel
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.CompleteAndConfirmFormModel
import CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import models.CompleteAndConfirmResponseModel
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey
import models.NewKeeperDetailsViewModel
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.VehicleTaxOrSornFormModel
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import models.AllCacheKeys
import NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.{TraderDetailsModel, VehicleDetailsModel}
import VehicleDetailsModel.VehicleLookupDetailsCacheKey
import utils.helpers.Config

class AcquireSuccess @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
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
        Ok(views.html.acquire.acquire_success(AcquireCompletionViewModel(vehicleDetailsModel,
          traderDetailsModel, newKeeperDetailsModel, completeAndConfirmModel, taxOrSornModel, responseModel)))
      case _ =>
        Logger.warn("Missing cookies in cache. Acquire was successful, however cannot display success page. " +
          "Redirecting to BeforeYouStart")
        Redirect(routes.BeforeYouStart.present())
    }
  }

  def buyAnother = Action { implicit request =>
    val result = for {
      acquireCompletionViewModel <- request.cookies.getModel[TraderDetailsModel]
    } yield Redirect(routes.VehicleLookup.present())
      .discardingCookies(Set(
        NewKeeperDetailsCacheKey,
        VehicleLookupDetailsCacheKey,
        VehicleLookupFormModelCacheKey,
        CompleteAndConfirmCacheKey,
        PrivateKeeperDetailsCacheKey,
        BusinessKeeperDetailsCacheKey,
        AcquireCompletionResponseCacheKey,
        VehicleTaxOrSornCacheKey
      ))
    result getOrElse {
      Logger.warn("Missing cookies in cache. Redirecting to BeforeYouStart")
      Redirect(routes.BeforeYouStart.present())
    }
  }

  def finish = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(AllCacheKeys)
  }
}
