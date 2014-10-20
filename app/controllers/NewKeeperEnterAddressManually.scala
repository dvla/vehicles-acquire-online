package controllers

import com.google.inject.Inject
import models.BusinessKeeperDetailsFormModel
import models.{NewKeeperEnterAddressManuallyViewModel, NewKeeperEnterAddressManuallyFormModel}
import models.PrivateKeeperDetailsFormModel
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContent, Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichForm, RichCookies, RichResult}
import common.model.AddressModel
import common.model.VehicleDetailsModel
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.new_keeper_enter_address_manually
import models.NewKeeperDetailsViewModel.createNewKeeper

final class NewKeeperEnterAddressManually @Inject()()
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config) extends Controller {

  private[controllers] val form = Form(
    NewKeeperEnterAddressManuallyFormModel.Form.Mapping
  )

  private final val KeeperDetailsNotInCacheMessage = "Failed to find keeper details in cache. " +
    "Now redirecting to vehicle lookup."
  private final val PrivateAndBusinessKeeperDetailsBothInCacheMessage = "Both private and business keeper details " +
    "found in cache. This is an error condition. Now redirecting to vehicle lookup."
  private final val VehicleDetailsNotInCacheMessage = "Failed to find vehicle details in cache. " +
    "Now redirecting to vehicle lookup"

  private def switch[R](request: Request[AnyContent],
                        onPrivate: PrivateKeeperDetailsFormModel => R,
                        onBusiness: BusinessKeeperDetailsFormModel => R,
                        onError: String => R): R = {
    val privateKeeperDetailsOpt = request.cookies.getModel[PrivateKeeperDetailsFormModel]
    val businessKeeperDetailsOpt = request.cookies.getModel[BusinessKeeperDetailsFormModel]
    (privateKeeperDetailsOpt, businessKeeperDetailsOpt) match {
      case (Some(privateKeeperDetails), Some(businessKeeperDetails)) => onError(PrivateAndBusinessKeeperDetailsBothInCacheMessage)
      case (Some(privateKeeperDetails), _) => onPrivate(privateKeeperDetails)
      case (_, Some(businessKeeperDetails)) => onBusiness(businessKeeperDetails)
      case _ => onError(KeeperDetailsNotInCacheMessage)
    }
  }

  private def error(message: String): Result = {
    Logger.error(message)
    Redirect(routes.VehicleLookup.present())
  }

  def present = Action { implicit request =>
    switch(request,
      { privateKeeperDetails => openView(privateKeeperDetails.postcode) },
      { businessKeeperDetails => openView(businessKeeperDetails.postcode) },
      message => error(message)
    )
  }

  private def openView(postcode: String)
                      (implicit request: Request[_]) = {
    request.cookies.getModel[VehicleDetailsModel] match {
      case Some(vehicleDetails) =>
        Ok(new_keeper_enter_address_manually(
          NewKeeperEnterAddressManuallyViewModel(form.fill(), vehicleDetails),
          postcode
        ))
      case _ => error(VehicleDetailsNotInCacheMessage)
    }
  }

  private def handleInvalidForm(invalidForm: Form[NewKeeperEnterAddressManuallyFormModel],
                                postcode: String)
                               (implicit request: Request[_]) = {
    request.cookies.getModel[VehicleDetailsModel] match {
      case Some(vehicleDetails) =>
        BadRequest(new_keeper_enter_address_manually(
          NewKeeperEnterAddressManuallyViewModel(formWithReplacedErrors(invalidForm), vehicleDetails),
          postcode)
        )
      case _ => error(VehicleDetailsNotInCacheMessage)
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        switch(request,
          { privateKeeperDetails => handleInvalidForm(invalidForm, privateKeeperDetails.postcode) },
          { businessKeeperDetails => handleInvalidForm(invalidForm, businessKeeperDetails.postcode) },
          message => error(message)
        ),
      validForm =>
        switch(request,
        { privateKeeperDetails => {
          val keeperAddress = AddressModel.from(validForm.addressAndPostcodeModel, privateKeeperDetails.postcode)
          createNewKeeper(keeperAddress) match {
            case Some(keeperDetails) => Redirect(routes.VehicleTaxOrSorn.present()).withCookie(validForm).withCookie(keeperDetails)
            case _ => error("No new keeper details found in cache, redirecting to vehicle lookup")
          }
        }
        },
        { businessKeeperDetails => {
          val keeperAddress = AddressModel.from(validForm.addressAndPostcodeModel, businessKeeperDetails.postcode)
            createNewKeeper(keeperAddress) match {
            case Some(keeperDetails) => Redirect(routes.VehicleTaxOrSorn.present()).withCookie(validForm).withCookie(keeperDetails)
            case _ => error("No new keeper details found in cache, redirecting to vehicle lookup")
          }
        }
        },
        message => error(message)
        )
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