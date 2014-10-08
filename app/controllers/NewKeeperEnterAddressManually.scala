package controllers

import com.google.inject.Inject
import models.BusinessKeeperDetailsFormModel
import models.NewKeeperEnterAddressManuallyFormModel
import models.PrivateKeeperDetailsFormModel
import models.SetupTradeDetailsFormModel
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichForm, RichCookies}
import common.model.{TraderDetailsModel, AddressModel}
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.new_keeper_enter_address_manually

final class NewKeeperEnterAddressManually @Inject()()
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config) extends Controller {

  private[controllers] val form = Form(
    NewKeeperEnterAddressManuallyFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    val privateKeeperDetailsOpt = request.cookies.getModel[PrivateKeeperDetailsFormModel]
    val businessKeeperDetailsOpt = request.cookies.getModel[BusinessKeeperDetailsFormModel]
    (privateKeeperDetailsOpt, businessKeeperDetailsOpt) match {
      case (Some(privateKeeperDetails), _) =>
        Ok(new_keeper_enter_address_manually(form.fill(), privateKeeperDetails.postcode))
      case (_, Some(businessKeeperDetails)) =>
        Ok(new_keeper_enter_address_manually(form.fill(), businessKeeperDetails.postcode))
      case _ =>
        Logger.warn("Failed to find a cookie for the new keeper. Now redirecting to vehicle lookup.")
        Redirect(routes.VehicleLookup.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        val privateKeeperDetailsOpt = request.cookies.getModel[PrivateKeeperDetailsFormModel]
        val businessKeeperDetailsOpt = request.cookies.getModel[BusinessKeeperDetailsFormModel]
        (privateKeeperDetailsOpt, businessKeeperDetailsOpt) match {
          case (Some(privateKeeperDetails), _) =>
            BadRequest(new_keeper_enter_address_manually(formWithReplacedErrors(invalidForm), privateKeeperDetails.postcode))
          case (_, Some(businessKeeperDetails)) =>
            BadRequest(new_keeper_enter_address_manually(formWithReplacedErrors(invalidForm), businessKeeperDetails.postcode))
          case _ =>
            Logger.warn("Failed to find a cookie for the new keeper. Now redirecting to vehicle lookup.")
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetails) =>
            val traderAddress = AddressModel.from(
              validForm.addressAndPostcodeModel,
              setupTradeDetails.traderPostcode
            )
            val traderDetailsModel = TraderDetailsModel(
              traderName = setupTradeDetails.traderBusinessName,
              traderAddress = traderAddress
            )

            // Redirect to the next screen in the workflow
            Redirect(routes.CompleteAndConfirm.present())//.
//              withCookie(validForm).
//              withCookie(traderDetailsModel)
          case None =>
            Logger.debug("Failed to find dealer name in cache on submit, redirecting")
            Redirect(routes.SetUpTradeDetails.present())
        }
    )
  }

  private def formWithReplacedErrors(form: Form[NewKeeperEnterAddressManuallyFormModel]) =
    form.replaceError(
      "addressAndPostcode.addressLines.buildingNameOrNumber",
      FormError("addressAndPostcode.addressLines", "error.address.buildingNameOrNumber.invalid")
    ).replaceError(
        "addressAndPostcode.addressLines.postTown",
        FormError("addressAndPostcode.addressLines", "error.address.postTown")
      ).replaceError(
        "addressAndPostcode.postcode",
        FormError("addressAndPostcode.postcode", "error.address.postcode.invalid")
      ).distinctErrors
}
