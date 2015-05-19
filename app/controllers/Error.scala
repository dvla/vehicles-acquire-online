package controllers

import com.google.inject.Inject
import models.CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.LogFormats.logMessage
import utils.helpers.{CookieHelper, Config}

class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  def present(exceptionDigest: String) = Action { implicit request =>
    Logger.error(logMessage("Error - Displaying generic error page", request.cookies.trackingId(),
      Seq(exceptionDigest)))
    Ok(views.html.acquire.error(exceptionDigest)).discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey)
  }

  def submit(exceptionDigest: String) = Action.async { implicit request =>
    Logger.error(logMessage("Error submit called - now removing full set of cookies and redirecting to Start page.",
      request.cookies.trackingId(), Seq(exceptionDigest)))

    CookieHelper.discardAllCookies
  }
}