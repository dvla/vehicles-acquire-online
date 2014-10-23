package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import play.api.Logger
import models.PrivateKeeperDetailsFormModel
import models.PrivateKeeperDetailsFormModel.Form.DriverNumberId
import models.PrivateKeeperDetailsFormModel.Form.EmailId
import models.PrivateKeeperDetailsFormModel.Form.FirstNameId
import models.PrivateKeeperDetailsFormModel.Form.LastNameId
import models.PrivateKeeperDetailsFormModel.Form.PostcodeId
import play.api.data.{FormError, Form}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.VehicleDetailsModel
import common.clientsidesession.CookieImplicits.RichCookies
import common.views.helpers.FormExtensions.formBinding
import common.clientsidesession.CookieImplicits.{RichForm, RichResult}
import utils.helpers.Config

final class PrivateKeeperDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  private[controllers] val form = Form(
    PrivateKeeperDetailsFormModel.Form.Mapping
  )

  private final val CookieErrorMessage = "Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails."

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleDetailsModel] match {
      case Some(vehicleDetails) => Ok(views.html.acquire.private_keeper_details(vehicleDetails, form.fill()))
      case _ => redirectToSetupTradeDetails(CookieErrorMessage)
    }
  }

  def submit = Action { implicit request =>
      request.cookies.getModel[VehicleDetailsModel] match {
        case Some(vehicleDetails) =>
          form.bindFromRequest.fold(
            invalidForm => BadRequest(views.html.acquire.private_keeper_details(vehicleDetails, formWithReplacedErrors(invalidForm))),
            validForm => Redirect(routes.NewKeeperChooseYourAddress.present()).withCookie(validForm))
        case _ => redirectToSetupTradeDetails(CookieErrorMessage)
      }
  }

  private def formWithReplacedErrors(form: Form[PrivateKeeperDetailsFormModel]) = {
    form.replaceError(
//      FirstNameId,
//      FormError(key = FirstNameId, message = "error.validFirstName", args = Seq.empty)
//    ).
//      replaceError(
        LastNameId,
        FormError(key = LastNameId,message = "error.validLastName", args = Seq.empty)
      ).
      replaceError(
        DriverNumberId,
        FormError(key = DriverNumberId, message = "error.validDriverNumber", args = Seq.empty)
      ).
      replaceError(
        PostcodeId,
        FormError(key = PostcodeId, message = "error.restricted.validPostcode", args = Seq.empty)
      ).distinctErrors
  }

  private def redirectToSetupTradeDetails(message:String) = {
    Logger.warn(message)
    Redirect(routes.SetUpTradeDetails.present())
  }
}