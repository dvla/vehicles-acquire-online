package controllers

import com.google.inject.Inject
import models.{NewKeeperDetailsViewModel, VehicleTaxOrSornViewModel, VehicleTaxOrSornFormModel}
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel
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
    val vehicleDetailsOpt = request.cookies.getModel[VehicleDetailsModel]
    (newKeeperDetailsOpt, vehicleDetailsOpt) match {
      case (Some(newKeeperDetails), Some(vehicleDetails)) =>
        Ok(vehicle_tax_or_sorn(VehicleTaxOrSornViewModel(form.fill(), vehicleDetails, newKeeperDetails)))
      case _ => redirectToVehicleLookup(NoCookiesFoundMessage)
    }
  }

  private def redirectToVehicleLookup(message: String) = {
    Logger.warn(message)
    Redirect(routes.VehicleLookup.present())
  }

  def back = Action { implicit request =>
    request.cookies.getModel[NewKeeperDetailsViewModel] match {
      case Some(keeperDetails) =>
        if (keeperDetails.address.uprn.isDefined) Redirect(routes.NewKeeperChooseYourAddress.present())
        else Redirect(routes.NewKeeperEnterAddressManually.present())
      case None => Redirect(routes.VehicleLookup.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => { // Note this code should never get executed as only an optional checkbox is posted
        val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
        val vehicleDetailsOpt = request.cookies.getModel[VehicleDetailsModel]
        (newKeeperDetailsOpt, vehicleDetailsOpt) match {
          case (Some(newKeeperDetails), Some(vehicleDetails)) =>
            BadRequest(vehicle_tax_or_sorn(VehicleTaxOrSornViewModel(form.fill(), vehicleDetails, newKeeperDetails)))
          case _ => redirectToVehicleLookup(NoCookiesFoundMessage)
        }
      },
      validForm => Redirect(routes.CompleteAndConfirm.present()).withCookie(validForm)
    )
  }
}