package controllers

import com.google.inject.Inject
import models.AllCacheKeys
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichResult
import common.clientsidesession.CookieImplicits.RichCookies
import common.LogFormats.logMessage
import utils.helpers.Config

class BeforeYouStart @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.acquire.before_you_start()).
      withNewSession.
      discardingCookies(AllCacheKeys)
  }

  def submit = Action { implicit request =>
    Logger.debug(logMessage(s"Redirecting to ${routes.SetUpTradeDetails.present()}", request.cookies.trackingId()))
    Redirect(routes.SetUpTradeDetails.present())
  }
}