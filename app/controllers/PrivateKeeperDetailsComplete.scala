package controllers

import models.{PrivateKeeperDetailsCompleteFormModel, PrivateKeeperDetailsCompleteViewModel, PrivateKeeperDetailsFormModel}
import views.html.acquire.private_keeper_details_complete
import com.google.inject.Inject
import play.api.mvc.{Request, Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import play.api.data.{FormError, Form}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import play.api.Logger
import models.PrivateKeeperDetailsFormModel.Form.ConsentId
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding

class PrivateKeeperDetailsComplete @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                               config: Config) extends Controller {

  private[controllers] val form = Form(
    PrivateKeeperDetailsCompleteFormModel.Form.Mapping
  )

  def present = Action { implicit request =>

    request.cookies.getModel[PrivateKeeperDetailsFormModel] match {
      case Some(privateKeeperDetails) => {
        Ok(private_keeper_details_complete(PrivateKeeperDetailsCompleteViewModel(
          form.fill(), null, null
        )))
      }
      case _ =>
        Logger.warn("Did not find PrivateKeeperDetails cookie. Now redirecting to SetUpTradeDetails.")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        BadRequest(private_keeper_details_complete(PrivateKeeperDetailsCompleteViewModel(
          invalidForm, null, null
        ))),
      validForm => Redirect(routes.NotImplemented.present()).withCookie(validForm)
    )
  }

  private def formWithReplacedErrors(form: Form[PrivateKeeperDetailsCompleteFormModel])(implicit request: Request[_]) = {
    form.replaceError(
      ConsentId,
      "error.required",
      FormError(key = ConsentId,
        message = "acquire_keeperdetailscomplete.consentError",
        args = Seq.empty)
    ).distinctErrors
  }

}
