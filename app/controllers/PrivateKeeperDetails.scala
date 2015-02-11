package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import play.api.Logger
import models.CookiePrefix
import models.PrivateKeeperDetailsFormModel
import models.PrivateKeeperDetailsFormModel.Form.DriverNumberId
import models.PrivateKeeperDetailsFormModel.Form.EmailId
import models.PrivateKeeperDetailsFormModel.Form.LastNameId
import models.PrivateKeeperDetailsFormModel.Form.PostcodeId
import play.api.data.{FormError, Form}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.VehicleAndKeeperDetailsModel
import common.clientsidesession.CookieImplicits.RichCookies
import common.views.helpers.FormExtensions.formBinding
import common.clientsidesession.CookieImplicits.{RichForm, RichResult}
import common.services.DateService
import utils.helpers.Config
import common.model.NewKeeperChooseYourAddressFormModel.newKeeperChooseYourAddressCacheKey

class PrivateKeeperDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       dateService: DateService,
                                       config: Config) extends Controller {

  private[controllers] val form = Form(
    PrivateKeeperDetailsFormModel.Form.detailMapping
  )

  private final val CookieErrorMessage = "Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails."

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(vehicleAndKeeperDetails) => Ok(views.html.acquire.private_keeper_details(vehicleAndKeeperDetails, form.fill()))
      case _ => redirectToSetupTradeDetails(CookieErrorMessage)
    }
  }

  def submit = Action { implicit request =>
      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
        case Some(vehicleAndKeeperDetails) =>
          form.bindFromRequest.fold(
            invalidForm => BadRequest(
              views.html.acquire.private_keeper_details(vehicleAndKeeperDetails, formWithReplacedErrors(invalidForm))
            ),
            validForm => Redirect(routes.NewKeeperChooseYourAddress.present()).withCookie(validForm)
                          .discardingCookie(newKeeperChooseYourAddressCacheKey))
        case _ => redirectToSetupTradeDetails(CookieErrorMessage)
      }
  }

  private def formWithReplacedErrors(form: Form[PrivateKeeperDetailsFormModel]) = {
    form.replaceError(
        LastNameId, FormError(key = LastNameId,message = "error.validLastName", args = Seq.empty)
      ).replaceError(
        DriverNumberId, FormError(key = DriverNumberId, message = "error.validDriverNumber", args = Seq.empty)
      ).replaceError(
        EmailId, FormError(key = EmailId, message = "error.email", args = Seq.empty)
      ).replaceError(
        PostcodeId, FormError(key = PostcodeId, message = "error.restricted.validPostcode", args = Seq.empty)
      ).distinctErrors
  }

  private def redirectToSetupTradeDetails(message:String) = {
    Logger.warn(message)
    Redirect(routes.SetUpTradeDetails.present())
  }
}
