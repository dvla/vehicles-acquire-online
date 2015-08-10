package controllers

import com.google.inject.Inject
import controllers.routes.VehicleLookup
import play.api.Logger
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.LogFormats.{DVLALogger}
import utils.helpers.Config
import models.AcquireCacheKeyPrefix.CookiePrefix

class MicroServiceError @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                        config: Config) extends Controller with DVLALogger {
  private final val DefaultRedirectUrl = VehicleLookup.present().url

  def present = Action { implicit request =>
    logMessage(request.cookies.trackingId(),Debug,s"MicroService error page")

    val referer = request.headers.get(REFERER).getOrElse(DefaultRedirectUrl)
    logMessage(request.cookies.trackingId(),Debug,s"Referer ${referer}")

    ServiceUnavailable(views.html.acquire.micro_service_error()).
      // Save the previous page URL (from the referer header) into a cookie.
      withCookie(MicroServiceError.MicroServiceErrorRefererCacheKey, referer)
  }

  def back = Action { implicit request =>
    val referer: String = request.cookies.getString(MicroServiceError.MicroServiceErrorRefererCacheKey)
      .getOrElse(DefaultRedirectUrl)
    logMessage(request.cookies.trackingId(),Debug,s"Microservice error page referer ${referer}")
    Redirect(referer).discardingCookie(MicroServiceError.MicroServiceErrorRefererCacheKey)
  }
}

object MicroServiceError {
  final val MicroServiceErrorRefererCacheKey = s"${CookiePrefix}msError"
}
