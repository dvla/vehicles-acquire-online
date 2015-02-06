package controllers

import com.google.inject.Inject
import models.CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey
import models.NewKeeperEnterAddressManuallyFormModel
import models.{NewKeeperDetailsViewModel, VehicleTaxOrSornViewModel, VehicleTaxOrSornFormModel}
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import utils.helpers.Config
import views.html.acquire.vehicle_tax_or_sorn

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
            BadRequest(vehicle_tax_or_sorn(VehicleTaxOrSornViewModel(form.fill(), vehicleAndKeeperDetails, newKeeperDetails)))
          case _ => redirectToVehicleLookup(NoCookiesFoundMessage)
        }
      },
      validForm => Redirect(routes.CompleteAndConfirm.present())
        .withCookie(validForm)
        .withCookie(AllowGoingToCompleteAndConfirmPageCacheKey, "true")
    )
  }
}