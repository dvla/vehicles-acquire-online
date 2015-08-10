package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import common.clientsidesession.CookieImplicits.RichCookies

/* Controller for redirecting people to the start page if the enter the application using the url "/" */
class Application @Inject()(implicit config: Config, clientSideSessionFactory: ClientSideSessionFactory) extends Controller with DVLALogger {
  private final val startUrl: String = config.startUrl

  def index = Action { implicit request =>
    logMessage(request.cookies.trackingId(),Debug,s"Redirecting to $startUrl...")
    Redirect(startUrl)
  }
}