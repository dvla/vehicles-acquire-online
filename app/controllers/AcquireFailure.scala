package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import models.AcquireCompletionViewModel.AcquireCompletionCacheKey
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import models.NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{VehicleDetailsModel, TraderDetailsModel}
import utils.helpers.Config
import models.{CompleteAndConfirmFormModel, NewKeeperDetailsViewModel, AcquireCompletionViewModel}

final class AcquireFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    val result = for {
      acquireCompletionViewModel <- request.cookies.getModel[AcquireCompletionViewModel]
    } yield
      Ok(views.html.acquire.acquire_failure(acquireCompletionViewModel))

    result getOrElse {
      Logger.warn("missing cookies in cache. Acquire failed, however cannot display failure page")
      Redirect(routes.BeforeYouStart.present())
    }
  }

  def buyAnother = Action { implicit request =>
    val result = for {
      traderDetails <- request.cookies.getModel[TraderDetailsModel]
    } yield Redirect(routes.VehicleLookup.present())
        .discardingCookies(Set(
        NewKeeperDetailsCacheKey,
        VehicleLookupDetailsCacheKey,
        VehicleLookupFormModelCacheKey,
        CompleteAndConfirmCacheKey,
        PrivateKeeperDetailsCacheKey,
        BusinessKeeperDetailsCacheKey,
        AcquireCompletionCacheKey
      ))
    result getOrElse {
      Logger.warn("missing cookies in cache.")
      Redirect(routes.BeforeYouStart.present())
    }
  }

  def finish = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present())
        .discardingCookies(Set(
        NewKeeperDetailsCacheKey,
        VehicleLookupDetailsCacheKey,
        VehicleLookupFormModelCacheKey,
        CompleteAndConfirmCacheKey,
        PrivateKeeperDetailsCacheKey,
        BusinessKeeperDetailsCacheKey,
        AcquireCompletionCacheKey
      ))
  }
}
