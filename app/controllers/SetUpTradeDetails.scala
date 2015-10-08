package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import play.api.data.Form
import play.api.mvc.{Result, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.controllers.SetUpTradeDetailsBase
import common.LogFormats.DVLALogger
import common.model.SetupTradeDetailsFormModel
import utils.helpers.Config

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends SetUpTradeDetailsBase with DVLALogger {

  override def presentResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result =
    Ok(views.html.acquire.setup_trade_details(model))

  override def invalidFormResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result =
    BadRequest(views.html.acquire.setup_trade_details(model))

  override def success(implicit request: Request[_]): Result = {
    logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.NewKeeperChooseYourAddress.present()}")
    Redirect(routes.BusinessChooseYourAddress.present())
  }

  def reset = play.api.mvc.Action { implicit request =>
    logMessage(request.cookies.trackingId(), Info, s"Reset trader details")
    Ok(views.html.acquire.setup_trade_details(form))
      .discardingCookies(models.TraderDetailsCacheKeys)
  }
}