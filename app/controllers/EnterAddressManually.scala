package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.EnterAddressManuallyFormModel
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichForm, RichCookies, RichResult}
import common.LogFormats.DVLALogger
import common.model.{VmAddressModel, SetupTradeDetailsFormModel, TraderDetailsModel}
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.enter_address_manually

class EnterAddressManually @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller with DVLALogger {

  private[controllers] val form = Form(
    EnterAddressManuallyFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    request.cookies.getModel[SetupTradeDetailsFormModel] match {
      case Some(setupTradeDetails) =>
        logMessage(request.cookies.trackingId(), Info, "Presenting enter address manually view")
        Ok(enter_address_manually(form.fill(), traderPostcode = setupTradeDetails.traderPostcode))
      case None =>
        logMessage(request.cookies.trackingId(), Error,
          s"Failed to find dealer details, redirecting to ${routes.SetUpTradeDetails.present()}")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetails) =>
            BadRequest(enter_address_manually(formWithReplacedErrors(invalidForm), setupTradeDetails.traderPostcode))
          case None =>
            logMessage(request.cookies.trackingId(), Debug,
              s"Failed to find dealer details in cache, redirecting to ${routes.SetUpTradeDetails.present()}")
            Redirect(routes.SetUpTradeDetails.present())
        },
      validForm => request.cookies.getModel[SetupTradeDetailsFormModel] match {
        case Some(setupTradeDetails) =>
          val traderAddress = VmAddressModel.from(validForm.addressAndPostcodeModel,setupTradeDetails.traderPostcode)
          val traderDetailsModel = TraderDetailsModel(
            traderName = setupTradeDetails.traderBusinessName,
            traderAddress = traderAddress,
            traderEmail = setupTradeDetails.traderEmail
          )
          logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.VehicleLookup.present()}")
          Redirect(routes.VehicleLookup.present()).
            discardingCookie(BusinessChooseYourAddressCacheKey).
            withCookie(validForm).
            withCookie(traderDetailsModel)
        case None =>
          logMessage(request.cookies.trackingId(), Warn,
            "Failed to find dealer details in cache on submit, redirecting to SetUpTradeDetails")
          Redirect(routes.SetUpTradeDetails.present())
      }
    )
  }

  private def formWithReplacedErrors(form: Form[EnterAddressManuallyFormModel]) =
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