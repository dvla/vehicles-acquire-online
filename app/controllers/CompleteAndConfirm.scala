package controllers

import com.google.inject.Inject
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmViewModel
import models.NewKeeperDetailsViewModel
import models.PrivateKeeperDetailsFormModel.Form.ConsentId
import models.CompleteAndConfirmFormModel.Form.MileageId
import play.api.data.{FormError, Form}
import play.api.mvc.{Action, Controller}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import common.model.VehicleDetailsModel
import utils.helpers.Config
import views.html.acquire.complete_and_confirm

class CompleteAndConfirm @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                               dateService: DateService,
                                               config: Config) extends Controller {

  private[controllers] val form = Form(
    CompleteAndConfirmFormModel.Form.Mapping
  )

  private final val NoNewKeeperCookieMessage = "Did not find a new keeper details cookie in cache. " +
    "Now redirecting to Vehicle Lookup."

  private final val NoCookiesFoundMessage = "Failed to find new keeper details and or vehicle details in cache. " +
    "Now redirecting to vehicle lookup"

  def present = Action { implicit request =>
    val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
    val vehicleDetailsOpt = request.cookies.getModel[VehicleDetailsModel]
    (newKeeperDetailsOpt, vehicleDetailsOpt) match {
      case (Some(newKeeperDetails), Some(vehicleDetails)) =>
        Ok(complete_and_confirm(CompleteAndConfirmViewModel(form.fill(), vehicleDetails, newKeeperDetails), dateService))
      case _ => redirectToVehicleLookup(NoCookiesFoundMessage)
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
        val vehicleDetailsOpt = request.cookies.getModel[VehicleDetailsModel]
        (newKeeperDetailsOpt, vehicleDetailsOpt) match {
          case (Some(newKeeperDetails), Some(vehicleDetails)) =>
            BadRequest(complete_and_confirm(
              CompleteAndConfirmViewModel(formWithReplacedErrors(invalidForm), vehicleDetails, newKeeperDetails), dateService)
            )
          case _ => redirectToVehicleLookup(NoCookiesFoundMessage)
        }
      },
      validForm =>
        request.cookies.getModel[NewKeeperDetailsViewModel] match {
          case Some(newKeeperDetails) => Redirect(routes.AcquireSuccess.present()).withCookie(validForm)
          case _ => redirectToVehicleLookup(NoNewKeeperCookieMessage)
        }
    )
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

  private def formWithReplacedErrors(form: Form[CompleteAndConfirmFormModel]) = {
    form.replaceError(
      ConsentId,
      "error.required",
      FormError(key = ConsentId, message = "acquire_keeperdetailscomplete.consentError", args = Seq.empty)
    ).replaceError(
        MileageId,
        "error.number",
        FormError(key = MileageId, message = "acquire_privatekeeperdetailscomplete.mileage.validation", args = Seq.empty)
    ).distinctErrors
  }
}