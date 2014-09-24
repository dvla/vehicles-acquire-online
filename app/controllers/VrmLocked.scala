package controllers

import com.google.inject.Inject
import models.{VrmLockedViewModel, AllCacheKeys, VehicleLookupCacheKeys}
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.model.{TraderDetailsModel, BruteForcePreventionModel}
import utils.helpers.Config

final class VrmLocked @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config) extends Controller {

  def present = Action { implicit request =>
    request.cookies.getModel[BruteForcePreventionModel] match {
      case Some(viewModel) =>
        Logger.debug(s"VrmLocked - Displaying the vrm locked error page")
        Ok(views.html.acquire.vrm_locked(VrmLockedViewModel(viewModel.dateTimeISOChronology, DateTime.parse(viewModel.dateTimeISOChronology).getMillis)))
      case None =>
        Logger.debug("VrmLocked - Can't find cookie for BruteForcePreventionViewModel")
        Redirect(routes.VehicleLookup.present())
    }
  }

  def buyAnotherVehicle = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel] match {
      case (Some(traderDetails)) =>
        Redirect(routes.VehicleLookup.present()).
          discardingCookies(VehicleLookupCacheKeys)
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def exit = Action { implicit request =>
    Redirect(routes.BeforeYouStart.present()).discardingCookies(AllCacheKeys)
  }
}
