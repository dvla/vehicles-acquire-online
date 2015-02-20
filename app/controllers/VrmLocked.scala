package controllers

import com.google.inject.Inject
import models.{AllCacheKeys, VehicleLookupCacheKeys, VrmLockedViewModel}
import org.joda.time.DateTime
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.controllers.VrmLockedBase
import common.model.BruteForcePreventionModel
import common.model.TraderDetailsModel
import utils.helpers.Config

class VrmLocked @Inject()()(implicit protected override val clientSideSessionFactory: ClientSideSessionFactory,
                            config: Config) extends VrmLockedBase {

  protected override def presentResult(model: BruteForcePreventionModel)
                                      (implicit request: Request[_]): Result =
    Ok(views.html.acquire.vrm_locked(
      VrmLockedViewModel(model.dateTimeISOChronology, DateTime.parse(model.dateTimeISOChronology).getMillis)
    ))

  protected override def missingBruteForcePreventionCookie(implicit request: Request[_]): Result =
    Redirect(routes.VehicleLookup.present())

  protected override def tryAnotherResult(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case (Some(traderDetails)) =>
        Redirect(routes.VehicleLookup.present()).
          discardingCookies(VehicleLookupCacheKeys)
      case _ => Redirect(routes.SetUpTradeDetails.present())
    }

  protected override def exitResult(implicit request: Request[_]): Result =
    Redirect(routes.BeforeYouStart.present()).discardingCookies(AllCacheKeys)
}
