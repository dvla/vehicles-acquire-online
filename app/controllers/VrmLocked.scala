package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.{AllCacheKeys, VehicleLookupCacheKeys, VrmLockedViewModel}
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.controllers.VrmLockedBase
import common.LogFormats.logMessage
import common.model.BruteForcePreventionModel
import common.model.TraderDetailsModel
import utils.helpers.Config

class VrmLocked @Inject()()(implicit protected override val clientSideSessionFactory: ClientSideSessionFactory,
                            config: Config) extends VrmLockedBase {

  protected override def presentResult(model: BruteForcePreventionModel)
                                      (implicit request: Request[_]): Result = {
    Logger.warn(logMessage("Vrm locked", request.cookies.trackingId()))
    Ok(views.html.acquire.vrm_locked(
      VrmLockedViewModel(model.dateTimeISOChronology, DateTime.parse(model.dateTimeISOChronology).getMillis)
    ))
  }

  protected override def missingBruteForcePreventionCookie(implicit request: Request[_]): Result = {
    Logger.debug(logMessage(s"Redirecting to ${routes.VehicleLookup.present()}", request.cookies.trackingId()))
    Redirect(routes.VehicleLookup.present())
  }

  protected override def tryAnotherResult(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case (Some(traderDetails)) => {
        Logger.debug(logMessage(s"Redirecting to ${routes.VehicleLookup.present()}", request.cookies.trackingId()))
        Redirect(routes.VehicleLookup.present()).
          discardingCookies(VehicleLookupCacheKeys)
      }
      case _ => {
        Logger.debug(logMessage(s"Redirecting to ${routes.SetUpTradeDetails.present()}", request.cookies.trackingId()))
        Redirect(routes.SetUpTradeDetails.present())
      }
    }

  protected override def exitResult(implicit request: Request[_]): Result = {
    Logger.debug(logMessage(s"Redirecting to ${routes.BeforeYouStart.present()}", request.cookies.trackingId()))
    Redirect(routes.BeforeYouStart.present()).discardingCookies(AllCacheKeys)
  }
}
