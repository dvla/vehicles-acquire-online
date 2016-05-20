package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.AcquireCompletionViewModel
import models.AllCacheKeys
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmResponseModel
import models.SurveyRequestTriggerDateCacheKey
import models.VehicleNewKeeperCompletionCacheKeys
import models.VehicleTaxOrSornFormModel
import play.api.mvc.{Request, Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import common.LogFormats.DVLALogger
import common.model.{TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.model.NewKeeperDetailsViewModel
import common.services.DateService
import utils.helpers.Config

class AcquireSuccess @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                  config: Config,
                                  surveyUrl: SurveyUrl,
                                  dateService: DateService) extends Controller with DVLALogger {

  def present = Action { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[TraderDetailsModel],
      request.cookies.getModel[NewKeeperDetailsViewModel],
      request.cookies.getModel[CompleteAndConfirmFormModel],
      request.cookies.getModel[VehicleTaxOrSornFormModel],
      request.cookies.getModel[CompleteAndConfirmResponseModel]
      ) match {
      case (Some(vehicleAndKeeperDetailsModel), Some(traderDetailsModel), Some(newKeeperDetailsModel),
        Some(completeAndConfirmModel), Some(taxOrSornModel), Some(responseModel)) =>
        val msg = "User transaction completed successfully - now displaying the acquire success view"
        logMessage(request.cookies.trackingId(), Info, msg)
        Ok(views.html.acquire.acquire_success(
          AcquireCompletionViewModel(
            vehicleAndKeeperDetailsModel,
            traderDetailsModel,
            newKeeperDetailsModel,
            completeAndConfirmModel,
            taxOrSornModel,
            responseModel
          ),
          surveyUrl(request)
        ))
      case _ =>
        val msg = "Missing cookies in cache. Acquire was successful, however will not " +
        "display success page. Redirecting to BeforeYouStart"
        redirectToStart(msg)
    }
  }

  def buyAnother = Action { implicit request =>
    val result = for {
      acquireCompletionViewModel <- request.cookies.getModel[TraderDetailsModel]
    } yield Redirect(routes.VehicleLookup.present())
      .discardingCookies(VehicleNewKeeperCompletionCacheKeys)
    result getOrElse redirectToStart("Missing cookies in cache. Redirecting to BeforeYouStart")
  }

  def finish = Action { implicit request =>
    Redirect("https://www.gov.uk")
      .discardingCookies(AllCacheKeys)
      .withCookie(SurveyRequestTriggerDateCacheKey, dateService.now.getMillis.toString)
  }

  private def redirectToStart(message: String)
                             (implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(), Warn, message)
    Redirect(routes.BeforeYouStart.present())
  }
}

class SurveyUrl @Inject()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                          config: Config,
                          dateService: DateService) extends DVLALogger {

  def apply(request: Request[_]): Option[String] = {
    val url = config.surveyUrl
    request.cookies.getString(SurveyRequestTriggerDateCacheKey) match {
      case Some(lastSurveyMillis) =>
        if ((lastSurveyMillis.toLong + config.surveyInterval) < dateService.now.getMillis) {
          logMessage(request.cookies.trackingId(), Debug, s"Redirecting to survey $url")
          url
        }
        else None
      case None =>
        logMessage(request.cookies.trackingId(), Debug, s"Redirecting to survey $url")
        url
    }
  }
}
