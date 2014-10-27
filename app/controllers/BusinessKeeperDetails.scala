package controllers

import com.google.inject.Inject
import models.BusinessKeeperDetailsFormModel
import play.api.data.{FormError, Form}
import play.api.mvc.{Action, Controller}
import play.api.Logger
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.VehicleDetailsModel
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.views.helpers.FormExtensions.formBinding
import models.NewKeeperChooseYourAddressFormModel.NewKeeperChooseYourAddressCacheKey
import models.BusinessKeeperDetailsViewModel

class BusinessKeeperDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  private[controllers] val form = Form(
    BusinessKeeperDetailsFormModel.Form.Mapping
  )

  private final val CookieErrorMessage = "Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails."

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleDetailsModel] match {
      case Some(vehicleDetails) =>
        Ok(views.html.acquire.business_keeper_details(
          BusinessKeeperDetailsViewModel(form.fill(),vehicleDetails)
        ))
      case _ => redirectToSetupTradeDetails(CookieErrorMessage)
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[VehicleDetailsModel] match {
          case Some(vehicleDetails) =>
            BadRequest(views.html.acquire.business_keeper_details(
              BusinessKeeperDetailsViewModel(formWithReplacedErrors(invalidForm), vehicleDetails)
            ))
          case None => redirectToSetupTradeDetails(CookieErrorMessage)
        }
      },
      validForm => Redirect(routes.NewKeeperChooseYourAddress.present()).withCookie(validForm)
                    .discardingCookie(NewKeeperChooseYourAddressCacheKey)
    )
  }

  private def formWithReplacedErrors(form: Form[BusinessKeeperDetailsFormModel]) = {
    form.replaceError(
      BusinessKeeperDetailsFormModel.Form.BusinessNameId,
      FormError(key = BusinessKeeperDetailsFormModel.Form.BusinessNameId,message = "error.validBusinessKeeperName")
    ).replaceError(
        BusinessKeeperDetailsFormModel.Form.EmailId,
        FormError(key = BusinessKeeperDetailsFormModel.Form.EmailId,message = "error.email")
      ).replaceError(
        BusinessKeeperDetailsFormModel.Form.PostcodeId,
        FormError(key = BusinessKeeperDetailsFormModel.Form.PostcodeId,message = "error.restricted.validPostcode")
      ).distinctErrors
  }

  private def redirectToSetupTradeDetails(message:String) = {
    Logger.warn(message)
    Redirect(routes.SetUpTradeDetails.present())
  }
}