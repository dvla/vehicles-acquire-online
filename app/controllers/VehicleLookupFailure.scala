package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.VehicleLookupFormModel
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import play.api.Logger
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.controllers.VehicleLookupFailureBase
import common.LogFormats.logMessage
import common.model.TraderDetailsModel
import utils.helpers.Config

class VehicleLookupFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends VehicleLookupFailureBase[VehicleLookupFormModel] {

  override val vehicleLookupResponseCodeCacheKey: String = VehicleLookupResponseCodeCacheKey

  override def presentResult(model: VehicleLookupFormModel, responseCode: String)(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) =>
        Ok(views.html.acquire.vehicle_lookup_failure(
          data = model,
          responseCodeVehicleLookupMSErrorMessage = responseCode)
        )
      case _ => missingPresentCookieDataResult
    }

  override def missingPresentCookieDataResult()(implicit request: Request[_]): Result = {
    Logger.debug(logMessage(s"Redirecting to ${routes.SetUpTradeDetails.present()}", request.cookies.trackingId()))
    Redirect(routes.SetUpTradeDetails.present())
  }

  override def submitResult()(implicit request: Request[_]): Result =
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) => {
        Logger.debug(logMessage(s"Redirecting to ${routes.VehicleLookup.present()}", request.cookies.trackingId()))
        Redirect(routes.VehicleLookup.present())
      }
      case _ => missingSubmitCookieDataResult
    }

  override def missingSubmitCookieDataResult()(implicit request: Request[_]): Result = {
    Logger.debug(logMessage(s"Redirecting to ${routes.BeforeYouStart.present()}", request.cookies.trackingId()))
    Redirect(routes.BeforeYouStart.present())
  }

}
