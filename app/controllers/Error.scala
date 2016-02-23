package controllers

import com.google.inject.Inject
import models.CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.utils.helpers.CookieHelper
import utils.helpers.Config

class Error @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller with DVLALogger {

  def present(exceptionDigest: String) = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Error,
      "Error - Displaying generic error page",
      Some(Seq(exceptionDigest)))
    Ok(views.html.acquire.error(exceptionDigest)).discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey)
  }

  def submit(exceptionDigest: String) = Action { implicit request =>
    logMessage(request.cookies.trackingId(), Error,
      "Error submit called - now removing full set of cookies and redirecting to Start page.",
      Some(Seq(exceptionDigest))
    )

    CookieHelper.discardAllCookies(routes.BeforeYouStart.present)
  }
}