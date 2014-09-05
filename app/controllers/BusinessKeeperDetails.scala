package controllers

import com.google.inject.Inject
import play.api.data.Form
import play.api.mvc.{Action, Controller}
import play.api.Logger
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.VehicleDetailsModel
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import viewmodels.{BusinessKeeperDetailsViewModel, BusinessKeeperDetailsFormViewModel}

final class BusinessKeeperDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  private[controllers] val form = Form(
    BusinessKeeperDetailsFormViewModel.Form.Mapping
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
/*
        val formWithReplacedErrors = invalidForm.replaceError(
          TraderNameId,
          FormError(key = TraderNameId, message = "error.validTraderBusinessName", args = Seq.empty)
        ).replaceError(
            TraderPostcodeId,
            FormError(key = TraderPostcodeId, message = "error.restricted.validPostcode", args = Seq.empty)
          ).distinctErrors
*/
//        BadRequest(views.html.disposal_of_vehicle.setup_trade_details(formWithReplacedErrors))
        BadRequest("Bad request")
      },
//      validForm => Redirect(routes.BusinessChooseYourAddress.present()).withCookie(validForm)
      validForm => Ok(s"Success - you entered $validForm").withCookie(validForm)
    )
  }
}
