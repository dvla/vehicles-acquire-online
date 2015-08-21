package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessKeeperDetailsCacheKeys
import models.PrivateKeeperDetailsCacheKeys
import models.EnterAddressManuallyFormModel
import models.VehicleLookupFormModel.{Key, JsonFormat}
import models.{VehicleLookupFormModel, VehicleLookupViewModel}
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import play.api.data.{Form, FormError}
import play.api.mvc.{Result, Action, Request}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.controllers.VehicleLookupBase
import common.model.BruteForcePreventionModel
import common.model.TraderDetailsModel
import common.model.VehicleAndKeeperDetailsModel
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupDetailsDto
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupErrorMessage
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService
import utils.helpers.Config
import views.acquire.VehicleLookup.VehicleSoldTo_Private

class VehicleLookup @Inject()(implicit bruteForceService: BruteForcePreventionService,
                              vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
                              dateService: DateService,
                              clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends VehicleLookupBase[VehicleLookupFormModel] {

  override val form = Form(VehicleLookupFormModel.Form.Mapping)
  override val responseCodeCacheKey: String = VehicleLookupResponseCodeCacheKey

  override def vrmLocked(bruteForcePreventionModel: BruteForcePreventionModel, formModel: VehicleLookupFormModel)
                        (implicit request: Request[_]): Result = {
    logMessage(request.cookies.trackingId(), Warn, s"Redirecting to ${routes.VrmLocked.present()}")
    Redirect(routes.VrmLocked.present())
  }

  override def microServiceError(t: Throwable, formModel: VehicleLookupFormModel)
                                (implicit request: Request[_]): Result = {
    logMessage(request.cookies.trackingId(), Error, s"Redirecting to ${routes.MicroServiceError.present()}")
    Redirect(routes.MicroServiceError.present())
  }

  override def vehicleLookupFailure(responseCode: VehicleAndKeeperLookupErrorMessage, formModel: VehicleLookupFormModel)
                                   (implicit request: Request[_]): Result = {
    logMessage(request.cookies.trackingId(), Warn, s"Redirecting to ${routes.VehicleLookupFailure.present()}")
    Redirect(routes.VehicleLookupFailure.present())
  }

  override def presentResult(implicit request: Request[_]) =
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(traderDetails) =>
        Ok(views.html.acquire.vehicle_lookup(
          VehicleLookupViewModel(
            form.fill(),
            traderDetails.traderName,
            traderDetails.traderAddress.address,
            traderDetails.traderEmail
          )))
      case None =>
        logMessage(request.cookies.trackingId(), Warn,
          s"No trader details found, now redirecting to ${routes.SetUpTradeDetails.present()}")
        Redirect(routes.SetUpTradeDetails.present())
    }

  override def invalidFormResult(invalidForm: Form[VehicleLookupFormModel])
                                (implicit request: Request[_]): Future[Result] = Future.successful {
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(traderDetails) =>
        BadRequest(
          views.html.acquire.vehicle_lookup(
            VehicleLookupViewModel(
              formWithReplacedErrors(invalidForm),
              traderDetails.traderName,
              traderDetails.traderAddress.address,
              traderDetails.traderEmail
            )
          )
        )
      case None =>
        logMessage(request.cookies.trackingId(), Warn,
          s"No trader details found, now redirecting to ${routes.SetUpTradeDetails.present()}")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def back = Action { implicit request =>
    request.cookies.getModel[EnterAddressManuallyFormModel] match {
      case Some(manualAddress) =>
        logMessage(request.cookies.trackingId(), Warn, s"Redirecting to ${routes.EnterAddressManually.present()}")
        Redirect(routes.EnterAddressManually.present())
      case None =>
        logMessage(request.cookies.trackingId(), Warn, s"Redirecting to ${routes.BusinessChooseYourAddress.present()}")
        Redirect(routes.BusinessChooseYourAddress.present())
    }
  }

  override def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperLookupDetailsDto,
                                  formModel: VehicleLookupFormModel)
                                 (implicit request: Request[_]): Result = {
    val model = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)
    val disposed = model.keeperEndDate.isDefined
    val suppressed = model.suppressedV5Flag.getOrElse(false)

    (disposed, suppressed) match {
      case (_, true) =>
        logMessage(request.cookies.trackingId(), Warn,
          s"V5C is suppressed, now redirecting to ${routes.SuppressedV5C.present()}")
        Redirect(routes.SuppressedV5C.present()).withCookie(model)
      case (true, false) => vehicleDisposedResult(model, formModel.vehicleSoldTo)
      case (false, _) =>
        logMessage(request.cookies.trackingId(), Warn,
          s"Keeper is still on record, now redirecting to ${routes.KeeperStillOnRecord.present()}")
        Redirect(routes.KeeperStillOnRecord.present()).withCookie(model)
    }
  }

  private def vehicleDisposedResult(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                                    soldTo: String)(implicit request: Request[_]) = {
    val (call, discardedCookies) =
      if (soldTo == VehicleSoldTo_Private) {
        logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.PrivateKeeperDetails.present()}")
        (routes.PrivateKeeperDetails.present(), BusinessKeeperDetailsCacheKeys)
      }
      else {
        logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.BusinessKeeperDetails.present()}")
        (routes.BusinessKeeperDetails.present(), PrivateKeeperDetailsCacheKeys)
      }

    Redirect(call).
      discardingCookies(discardedCookies).
      withCookie(vehicleAndKeeperDetailsModel)
  }

  private def formWithReplacedErrors(invalidForm: Form[VehicleLookupFormModel]) =
    invalidForm.replaceError(
      VehicleLookupFormModel.Form.VehicleRegistrationNumberId,
      FormError(
        key = VehicleLookupFormModel.Form.VehicleRegistrationNumberId,
        message = "error.restricted.validVrnOnly",
        args = Seq.empty
      )
    ).replaceError(
        VehicleLookupFormModel.Form.VehicleSoldToId,
        FormError(
          key = VehicleLookupFormModel.Form.VehicleSoldToId,
          message = "error.restricted.validKeeperOption",
          args = Seq.empty
        )
      ).replaceError(
        VehicleLookupFormModel.Form.DocumentReferenceNumberId,
        FormError(
          key = VehicleLookupFormModel.Form.DocumentReferenceNumberId,
          message = "error.validDocumentReferenceNumber",
          args = Seq.empty
        )
      ).distinctErrors
}
