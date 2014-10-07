package controllers

import com.google.inject.Inject
import models.{BusinessKeeperDetailsFormModel, CompleteAndConfirmFormModel, CompleteAndConfirmViewModel, PrivateKeeperDetailsFormModel}
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
      case (Some(privateKeeperDetails), None) =>
        Ok(complete_and_confirm(CompleteAndConfirmViewModel(form.fill(), null, null), dateService))
      case (None, Some(businessKeeperDetails)) =>
        Ok(complete_and_confirm(CompleteAndConfirmViewModel(form.fill(), null, null), dateService))
      case _ =>
        Logger.warn("Did not find a new keeper details cookie. Now redirecting to Vehicle Lookup.")
        Redirect(routes.VehicleLookup.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        BadRequest(complete_and_confirm(CompleteAndConfirmViewModel(
          formWithReplacedErrors(invalidForm), null, null
        ), dateService)),
      validForm => Redirect(routes.NotImplemented.present()).withCookie(validForm)
    )
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