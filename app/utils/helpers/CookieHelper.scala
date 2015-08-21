package utils.helpers

import controllers.routes
import play.api.mvc.Results.Redirect
import play.api.mvc.{DiscardingCookie, RequestHeader, Result}
import models.SeenCookieMessageCacheKey
import uk.gov.dvla.vehicles.presentation.common.LogFormats.DVLALogger

object CookieHelper extends DVLALogger {
  def discardAllCookies(implicit request: RequestHeader): Result = {
    //logMessage(request.cookies.trackingId(),Warn,"Removing all cookies except seen cookie.")

    val discardingCookiesKeys = request.cookies.map(_.name).filter(_ != SeenCookieMessageCacheKey)
    val discardingCookies = discardingCookiesKeys.map(DiscardingCookie(_)).toSeq
    Redirect(routes.BeforeYouStart.present())
      .discardingCookies(discardingCookies: _*)
  }
}
