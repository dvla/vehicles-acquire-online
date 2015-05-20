package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichForm}
import common.model.NewKeeperEnterAddressManuallyFormModel
import common.LogFormats.logMessage
import common.clientsidesession.CookieImplicits.RichCookies
import common.controllers.NewKeeperEnterAddressManuallyBase
import common.model.NewKeeperEnterAddressManuallyViewModel
import common.model.VehicleAndKeeperDetailsModel

import utils.helpers.Config
import views.html.acquire.new_keeper_enter_address_manually

class NewKeeperEnterAddressManually @Inject()()
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config) extends NewKeeperEnterAddressManuallyBase {

  protected override def presentResult(model: VehicleAndKeeperDetailsModel, postcode: String,
                              form: Form[NewKeeperEnterAddressManuallyFormModel])
                             (implicit request: Request[_]): Result =
          Ok(new_keeper_enter_address_manually(NewKeeperEnterAddressManuallyViewModel(form.fill(), model), postcode))

  protected override def missingVehicleDetails(implicit request: Request[_]): Result = {
    Logger.debug(logMessage(s"Redirecting to ${routes.VehicleLookup.present()}", request.cookies.trackingId()))
      Redirect(routes.VehicleLookup.present())
  }

  protected override def invalidFormResult(model: VehicleAndKeeperDetailsModel, postcode: String,
                                  form: Form[NewKeeperEnterAddressManuallyFormModel])
                                 (implicit request: Request[_]): Result =
          BadRequest(new_keeper_enter_address_manually(
            NewKeeperEnterAddressManuallyViewModel(formWithReplacedErrors(form), model), postcode))

  protected override def success(implicit request: Request[_]): Result = {
    Logger.debug(logMessage(s"Redirecting to ${routes.VehicleTaxOrSorn.present()}", request.cookies.trackingId()))
          Redirect(routes.VehicleTaxOrSorn.present())
  }
}
