package controllers

import com.google.inject.Inject
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import models.AcquireCompletionViewModel
import models.AcquireCompletionViewModel.AcquireCompletionCacheKey
import models.CompleteAndConfirmFormModel
import models.NewKeeperDetailsViewModel
import NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.VehicleDetailsModel
import VehicleDetailsModel.VehicleLookupDetailsCacheKey
import utils.helpers.Config
import models.AllCacheKeys

final class AcquireSuccess @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>

    val result = for {
      acquireCompletionViewModel <- request.cookies.getModel[AcquireCompletionViewModel]
    } yield
      Ok(views.html.acquire.acquire_success(acquireCompletionViewModel))

    result getOrElse {
      Logger.warn("Missing cookies in cache. Acquire was successful, however cannot display success page. " +
        "Redirecting to BeforeYouStart")
      Redirect(routes.BeforeYouStart.present())
    }
  }

  def buyAnother = Action { implicit request =>
    val result = for {
      acquireCompletionViewModel <- request.cookies.getModel[AcquireCompletionViewModel]
    } yield Redirect(routes.VehicleLookup.present())
      .discardingCookies(Set(
        NewKeeperDetailsCacheKey,
        VehicleLookupDetailsCacheKey,
        VehicleLookupFormModelCacheKey,
        CompleteAndConfirmCacheKey,
        PrivateKeeperDetailsCacheKey,
        BusinessKeeperDetailsCacheKey,
        AcquireCompletionCacheKey,
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