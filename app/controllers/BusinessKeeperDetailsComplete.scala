package controllers

import com.google.inject.Inject
import models.{BusinessKeeperDetailsCompleteFormModel, BusinessKeeperDetailsFormModel, BusinessKeeperDetailsCompleteViewModel}
import models.PrivateKeeperDetailsFormModel.Form.ConsentId
import models.BusinessKeeperDetailsCompleteFormModel.Form.MileageId
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import play.api.mvc.{Action, Controller}
import play.api.data.{FormError, Form}
import play.api.Logger
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.clientsidesession.CookieImplicits.{RichForm, RichResult}
import common.views.helpers.FormExtensions.formBinding
import views.html.acquire.business_keeper_details_complete

class BusinessKeeperDetailsComplete @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                               dateService: DateService,
                                               config: Config) extends Controller {

  private[controllers] val form = Form(
    BusinessKeeperDetailsCompleteFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    request.cookies.getModel[BusinessKeeperDetailsFormModel] match {
      case Some(businessKeeperDetails) =>
        Ok(business_keeper_details_complete(BusinessKeeperDetailsCompleteViewModel(
          form.fill(), null, null
        ), dateService))
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
        ), dateService)),
      validForm => Redirect(routes.NotImplemented.present()).withCookie(validForm)
    )
  }

  private def formWithReplacedErrors(form: Form[BusinessKeeperDetailsCompleteFormModel]) = {
    form.replaceError(
      ConsentId,
      "error.required",
      FormError(key = ConsentId, message = "acquire_keeperdetailscomplete.consentError", args = Seq.empty)
    ).replaceError(
        MileageId,
        "error.number",
        FormError(key = MileageId, message = "acquire_businesskeeperdetailscomplete.mileage.validation", args = Seq.empty)
    ).distinctErrors
  }
}
