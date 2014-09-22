package controllers

import models.PrivateKeeperDetailsFormModel.Form._
import models._
import views.html.acquire.business_keeper_details_complete
import com.google.inject.Inject
import play.api.mvc.{Request, Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import play.api.data.{FormError, Form}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichForm, RichResult}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import models.BusinessKeeperDetailsCompleteFormModel.Form.MileageId

class BusinessKeeperDetailsComplete @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                               config: Config) extends Controller {

  private[controllers] val form = Form(
    BusinessKeeperDetailsCompleteFormModel.Form.Mapping
  )

  def present = Action { implicit request =>

    request.cookies.getModel[BusinessKeeperDetailsFormModel] match {
      case Some(businessKeeperDetails) => {
        Ok(business_keeper_details_complete(BusinessKeeperDetailsCompleteViewModel(
          form.fill(), null, null
        )))
      }
      case _ =>
        Logger.warn("Did not find BusinessKeeperDetails cookie. Now redirecting to SetUpTradeDetails.")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        BadRequest(business_keeper_details_complete(BusinessKeeperDetailsCompleteViewModel(
          formWithReplacedErrors(invalidForm), null, null
        ))),
      validForm => Redirect(routes.NotImplemented.present()).withCookie(validForm)
    )
  }

  private def formWithReplacedErrors(form: Form[BusinessKeeperDetailsCompleteFormModel])(implicit request: Request[_]) = {
    form.replaceError(ConsentId, "error.required", FormError(key = ConsentId, message = "acquire_keeperdetailscomplete.consentError", args = Seq.empty))
      .replaceError(MileageId, "error.number", FormError(key = MileageId, message = "acquire_businesskeeperdetailscomplete.mileage.validation", args = Seq.empty))
      .distinctErrors
  }

}
