package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey
import models._
import play.api.Logger
import play.api.data.{FormError, Form}
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.model.{NewKeeperDetailsViewModel, NewKeeperEnterAddressManuallyFormModel, VehicleAndKeeperDetailsModel}
import models.VehicleTaxOrSornFormModel.Form.{SelectId, SornVehicleId, SornFormError}
import utils.helpers.Config
import views.html.acquire.vehicle_tax_or_sorn
import common.views.helpers.FormExtensions.formBinding

class VehicleTaxOrSorn @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends Controller {

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
        Ok(vehicle_tax_or_sorn(VehicleTaxOrSornViewModel(form.fill(), vehicleAndKeeperDetails, newKeeperDetails)))
      case _ =>
        redirectToVehicleLookup(NoCookiesFoundMessage)
    }
  }

  private def redirectToVehicleLookup(message: String) = {
    Logger.warn(message)
    Redirect(routes.VehicleLookup.present())
  }

  def back = Action { implicit request =>
    request.cookies.getModel[NewKeeperEnterAddressManuallyFormModel] match {
      case Some(manualAddress) =>
        Redirect(routes.NewKeeperEnterAddressManually.present())
      case None => Redirect(routes.NewKeeperChooseYourAddress.present())
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
//        val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
//        val vehicleAndKeeperDetailsOpt = request.cookies.getModel[VehicleAndKeeperDetailsModel]
//        if (validForm.select == "S" && validForm.sornVehicle.isDefined) {
          Redirect(routes.CompleteAndConfirm.present())
            .withCookie(validForm)
            .withCookie(AllowGoingToCompleteAndConfirmPageCacheKey, "true")
//        }else {
//          (newKeeperDetailsOpt, vehicleAndKeeperDetailsOpt) match {
//            case (Some(newKeeperDetails), Some(vehicleAndKeeperDetails)) =>
//              BadRequest(vehicle_tax_or_sorn(VehicleTaxOrSornViewModel(formWithReplacedErrors(form),
//              vehicleAndKeeperDetails, newKeeperDetails, error = false)))
//            case _ => redirectToVehicleLookup(NoCookiesFoundMessage)
//          }
//        }
      }
    )
  }

  private def formWithReplacedErrors(form: Form[VehicleTaxOrSornFormModel]): (Form[VehicleTaxOrSornFormModel], ErrorType) = {

    (form.replaceError(
      SornVehicleId, FormError(key = SornVehicleId,message = "error.sornVehicleid", args = Seq.empty)
    ).replaceError(
        SelectId, FormError(key = SelectId, message = "error.sornselectid", args = Seq.empty)
      ).replaceError(
        "",  FormError(key = SornFormError, message = "error.sornformerror", args = Seq.empty)
    ).distinctErrors, if (form.globalError.isDefined) NoSorn else NoSelection)
  }
}