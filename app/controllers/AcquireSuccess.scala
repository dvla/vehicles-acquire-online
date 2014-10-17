package controllers

import com.google.inject.Inject
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
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
import common.model.{TraderDetailsModel, VehicleDetailsModel}
import VehicleDetailsModel.VehicleLookupDetailsCacheKey
import utils.helpers.Config

final class AcquireSuccess @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>

    val result = for {
      acquireCompletionViewModel <- request.cookies.getModel[AcquireCompletionViewModel]
    } yield
      Ok(views.html.acquire.acquire_success(acquireCompletionViewModel))

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
}