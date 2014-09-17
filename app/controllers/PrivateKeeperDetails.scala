package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import play.api.data.{FormError, Form}
import models.PrivateKeeperDetailsFormModel
import models.PrivateKeeperDetailsFormModel.Form.{titleOptions,TitleId, FirstNameId, LastNameId, EmailId}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichForm, RichResult}

final class PrivateKeeperDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  private[controllers] val form = Form(
    PrivateKeeperDetailsFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleDetailsModel] match {
      case Some(vehicleDetails) =>
        Ok(views.html.acquire.private_keeper_details(vehicleDetails, form.fill(), titleOptions))
      case _ =>
        Logger.warn("Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails.")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action { implicit request =>
      request.cookies.getModel[VehicleDetailsModel] match {
        case Some(vehicleDetails) =>
          form.bindFromRequest.fold(
            invalidForm => {
              val formWithReplacedErrors = invalidForm.
                replaceError(TitleId, FormError(key = TitleId, message = "error.titleInvalid", args = Seq.empty)).
                replaceError(FirstNameId, FormError(key = FirstNameId, message = "error.validFirstName", args = Seq.empty)).
                replaceError(LastNameId, FormError(key = LastNameId, message = "error.validLastName", args = Seq.empty)).
                replaceError(EmailId, FormError(key = FirstNameId, message = "error.validEmail", args = Seq.empty)).distinctErrors
              BadRequest(views.html.acquire.private_keeper_details(vehicleDetails, formWithReplacedErrors, titleOptions))
            },
            validForm => Redirect(routes.PrivateKeeperDetailsComplete.present()).withCookie(validForm))
        case _ =>
          Logger.warn("Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails.")
          Redirect(routes.SetUpTradeDetails.present())
      }
  }
}
