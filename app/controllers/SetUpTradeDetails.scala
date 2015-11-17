package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.IdentifierCacheKey
import play.api.data.Form
import play.api.mvc.{Action, Result, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.controllers.SetUpTradeDetailsBase
import common.LogFormats.DVLALogger
import common.model.SetupTradeDetailsFormModel
import utils.helpers.Config

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends SetUpTradeDetailsBase with DVLALogger {

  override def presentResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result = {
    request.cookies.getString(IdentifierCacheKey) match {
      case Some(c) =>
        Redirect(routes.SetUpTradeDetails.ceg)
      case None =>
        logMessage(request.cookies.trackingId(), Info, "Presenting set up trade details view")
        Ok(views.html.acquire.setup_trade_details(model))
    }
  }

  override def invalidFormResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result =
    BadRequest(views.html.acquire.setup_trade_details(model))

  override def success(implicit request: Request[_]): Result = {
    logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.NewKeeperChooseYourAddress.present()}")
    Redirect(routes.BusinessChooseYourAddress.present())
  }

  def reset = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Info, s"Reset trader details")
    // Call presentResult directly as we don't want the form to be populated
    // before we've had a chance to discard the cookie.
    presentResult(form)
      .discardingCookies(models.TraderDetailsCacheKeys)
  }

  val identifier = "CEG"
  def ceg = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Info, s"Presenting set up trade details view for identifier ${identifier}")
    Ok(views.html.acquire.setup_trade_details(form.fill()))
      .withCookie(IdentifierCacheKey, identifier)
  }
}
