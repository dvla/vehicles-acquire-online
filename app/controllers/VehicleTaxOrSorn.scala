package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey
import models.ErrorType
import models.NoSelection
import models.NoSorn
import models.VehicleTaxOrSornFormModel
import models.VehicleTaxOrSornFormModel.Form.{SelectId, SornVehicleId, SornFormError}
import models.VehicleTaxOrSornViewModel
import play.api.data.{FormError, Form}
import play.api.mvc.{Request, Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.LogFormats.DVLALogger
import common.model.{NewKeeperDetailsViewModel, NewKeeperEnterAddressManuallyFormModel, VehicleAndKeeperDetailsModel}
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.vehicle_tax_or_sorn

class VehicleTaxOrSorn @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                   config: Config) extends Controller with DVLALogger {

  private[controllers] val form = Form(
    VehicleTaxOrSornFormModel.Form.Mapping
  )

  private final val NoCookiesFoundMessage = "Failed to find new keeper details and or vehicle details in cache. " +
    "Now redirecting to vehicle lookup"

  def present = Action { implicit request =>
    val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
    val vehicleAndKeeperDetailsOpt = request.cookies.getModel[VehicleAndKeeperDetailsModel]
    (newKeeperDetailsOpt, vehicleAndKeeperDetailsOpt) match {
      case (Some(newKeeperDetails), Some(vehicleAndKeeperDetails)) =>
        logMessage(request.cookies.trackingId(), Info, "Presenting vehicle tax or sorn view")
        Ok(vehicle_tax_or_sorn(VehicleTaxOrSornViewModel(form.fill(), vehicleAndKeeperDetails, newKeeperDetails)))
      case _ =>
        redirectToVehicleLookup(NoCookiesFoundMessage)
    }
  }

  private def redirectToVehicleLookup(message: String)
                                     (implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(), Warn, message)
    Redirect(routes.VehicleLookup.present())
  }

  def back = Action { implicit request =>
    request.cookies.getModel[NewKeeperEnterAddressManuallyFormModel] match {
      case Some(manualAddress) =>
        logMessage(request.cookies.trackingId(), Debug,
          s"Redirecting to ${routes.NewKeeperEnterAddressManually.present()}")
        Redirect(routes.NewKeeperEnterAddressManually.present())
      case None =>
        logMessage(request.cookies.trackingId(), Debug,
          s"Redirecting to ${routes.NewKeeperChooseYourAddress.present()}")
        Redirect(routes.NewKeeperChooseYourAddress.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => { // Note this code should never get executed as only an optional checkbox is posted
        val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
        val vehicleAndKeeperDetailsOpt = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        (newKeeperDetailsOpt, vehicleAndKeeperDetailsOpt) match {
          case (Some(newKeeperDetails), Some(vehicleAndKeeperDetails)) =>
            val (errorForm, error) = formWithReplacedErrors(invalidForm)
            BadRequest(vehicle_tax_or_sorn(VehicleTaxOrSornViewModel(errorForm,
              vehicleAndKeeperDetails, newKeeperDetails, error)))
          case _ => redirectToVehicleLookup(NoCookiesFoundMessage)
        }
      },
      validForm => {
        logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.CompleteAndConfirm.present()}")
          Redirect(routes.CompleteAndConfirm.present())
            .withCookie(validForm)
            .withCookie(AllowGoingToCompleteAndConfirmPageCacheKey, "true")

      }
    )
  }

  private def formWithReplacedErrors(form: Form[VehicleTaxOrSornFormModel]): (Form[VehicleTaxOrSornFormModel], ErrorType) = {
    (
      if ( form.data.get("select").exists(_ == "S") && form.globalError.isDefined) {
        form.replaceError(
          "", FormError(key = SornFormError, message = "error.sornformerror", args = Seq.empty)
        ).replaceError(
            SornVehicleId, FormError(key = SornVehicleId,message = "error.sornVehicleid", args = Seq.empty)
          ).replaceError(
            SelectId, FormError(key = SelectId, message = "error.sornselectid", args = Seq.empty)
          ).distinctErrors
      } else {
        form.replaceError(
          "", FormError(key = SornFormError, message = "error.nosornformerror", args = Seq.empty)
        ).replaceError(
            SornVehicleId, FormError(key = SornVehicleId,message = "error.sornVehicleid", args = Seq.empty)
          ).replaceError(
            SelectId, FormError(key = SelectId, message = "error.sornselectid", args = Seq.empty)
          ).distinctErrors
      }
      , if (form.globalError.isDefined) NoSorn else NoSelection)
  }
}