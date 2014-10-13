package controllers

import com.google.inject.Inject
import play.api.data.{FormError, Form}
import play.api.mvc.{Action, Controller}
import play.api.Logger
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.VehicleDetailsModel
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import models.{BusinessKeeperDetailsViewModel, BusinessKeeperDetailsFormModel}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding

final class BusinessKeeperDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  private[controllers] val form = Form(
    BusinessKeeperDetailsFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleDetailsModel] match {
      case Some(vehicleDetails) =>
        Ok(views.html.acquire.business_keeper_details(
          BusinessKeeperDetailsViewModel(
            form.fill(),
            vehicleDetails
          )
        ))
      case _ =>
        Logger.warn("Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails.")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[VehicleDetailsModel] match {
          case Some(vehicleDetails) =>
            val formWithReplacedErrors = invalidForm.
              replaceError(BusinessKeeperDetailsFormModel.Form.BusinessNameId, FormError(
                key = BusinessKeeperDetailsFormModel.Form.BusinessNameId,message = "error.validBusinessName"
            )).
              replaceError(BusinessKeeperDetailsFormModel.Form.PostcodeId, FormError(
                key = BusinessKeeperDetailsFormModel.Form.PostcodeId,message = "error.restricted.validPostcode"
            )).distinctErrors
            BadRequest(views.html.acquire.business_keeper_details(
              BusinessKeeperDetailsViewModel(
                formWithReplacedErrors,
                vehicleDetails
              )
            ))
          case None =>
            Logger.warn("Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails.")
            Redirect(routes.SetUpTradeDetails.present())
        }
      },
      validForm => Redirect(routes.NewKeeperChooseYourAddress.present()).withCookie(validForm)
    )
  }
}