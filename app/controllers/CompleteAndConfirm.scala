package controllers

import com.google.inject.Inject
import models.BusinessKeeperDetailsFormModel
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmViewModel
import models.NewKeeperDetailsViewModel
import models.PrivateKeeperDetailsFormModel
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
import utils.helpers.Config
import views.html.acquire.complete_and_confirm

class CompleteAndConfirm @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                               dateService: DateService,
                                               config: Config) extends Controller {

  private[controllers] val form = Form(
    CompleteAndConfirmFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    (request.cookies.getModel[PrivateKeeperDetailsFormModel], request.cookies.getModel[BusinessKeeperDetailsFormModel]) match {
      case (Some(privateKeeperDetails), _) =>
        Ok(complete_and_confirm(CompleteAndConfirmViewModel(form.fill(), null, null), dateService))
      case (_, Some(businessKeeperDetails)) =>
        Ok(complete_and_confirm(CompleteAndConfirmViewModel(form.fill(), null, null), dateService))
      case _ =>
        Logger.warn("Did not find a new keeper details cookie. Now redirecting to Vehicle Lookup.")
        Redirect(routes.VehicleLookup.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        (request.cookies.getModel[PrivateKeeperDetailsFormModel], request.cookies.getModel[BusinessKeeperDetailsFormModel]) match {
          case (Some(privateKeeperDetails), _) =>
            BadRequest(complete_and_confirm(CompleteAndConfirmViewModel(
              formWithReplacedErrors(invalidForm), null, null
            ), dateService))
          case (_, Some(businessKeeperDetails)) =>
            BadRequest(complete_and_confirm(CompleteAndConfirmViewModel(
              formWithReplacedErrors(invalidForm), null, null
            ), dateService))
          case _ =>
            Logger.warn("Did not find a new keeper details cookie on submit. Now redirecting to Vehicle Lookup.")
            Redirect(routes.VehicleLookup.present())
        },
      validForm =>
        (request.cookies.getModel[PrivateKeeperDetailsFormModel], request.cookies.getModel[BusinessKeeperDetailsFormModel]) match {
          case (Some(privateKeeperDetails), _) => Redirect(routes.NotImplemented.present()).withCookie(validForm)
          case (_, Some(businessKeeperDetails)) => Redirect(routes.NotImplemented.present()).withCookie(validForm)
          case _ =>
            Logger.warn("Did not find a new keeper details cookie on submit. Now redirecting to Vehicle Lookup.")
            Redirect(routes.VehicleLookup.present())
        }
    )
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