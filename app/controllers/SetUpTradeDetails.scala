package controllers

import com.google.inject.Inject
import models.SetupTradeDetailsFormModel
import models.SetupTradeDetailsFormModel.Form.{TraderNameId, TraderEmailId, TraderPostcodeId}
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichForm, RichResult}
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends Controller {

  private[controllers] val form = Form(
    SetupTradeDetailsFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    Ok(views.html.acquire.setup_trade_details(form.fill()))
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => BadRequest(views.html.acquire.setup_trade_details(formWithReplacedErrors(invalidForm))),
      validForm => Redirect(routes.BusinessChooseYourAddress.present()).withCookie(validForm)
    )
  }

  private def formWithReplacedErrors(form: Form[SetupTradeDetailsFormModel]) = {
    form.replaceError(
      TraderNameId, FormError(key = TraderNameId, message = "error.validBusinessName", args = Seq.empty)
    ).replaceError(
        TraderEmailId,FormError(key = TraderEmailId, message = "error.email", args = Seq.empty)
      ).replaceError(
        TraderPostcodeId, FormError(key = TraderPostcodeId, message = "error.restricted.validPostcode", args = Seq.empty)
      ).distinctErrors
  }
}