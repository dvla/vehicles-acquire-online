package controllers

import com.google.inject.Inject
import models.BusinessKeeperDetailsFormModel
import models.NewKeeperDetailsViewModel
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
                        onNeither: String => R): R = {
    val privateKeeperDetailsOpt = request.cookies.getModel[PrivateKeeperDetailsFormModel]
    val businessKeeperDetailsOpt = request.cookies.getModel[BusinessKeeperDetailsFormModel]
    (privateKeeperDetailsOpt, businessKeeperDetailsOpt) match {
      case (Some(privateKeeperDetails), Some(businessKeeperDetails)) => onNeither(PrivateAndBusinessKeeperDetailsBothInCacheMessage)
      case (Some(privateKeeperDetails), _) => onPrivate(privateKeeperDetails)
      case (_, Some(businessKeeperDetails)) => onBusiness(businessKeeperDetails)
      case _ => onNeither(KeeperDetailsNotInCacheMessage)
    }
  }

  private def neither(message: String): Result = {
    Logger.warn(message)
    Redirect(routes.VehicleLookup.present())
  }

  def present = Action { implicit request =>
    switch(request,
      { privateKeeperDetails => openView(privateKeeperDetails.postcode) },
      { businessKeeperDetails => openView(businessKeeperDetails.postcode) },
      message => neither(message)
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
      case _ => neither(VehicleDetailsNotInCacheMessage)
    }
  }

  private def handleInvalidForm(invalidForm: Form[NewKeeperEnterAddressManuallyFormModel],
                                postcode: String)
                               (implicit request: Request[_]) = {
    val vehicleDetails = request.cookies.getModel[VehicleDetailsModel]
    BadRequest(new_keeper_enter_address_manually(
      NewKeeperEnterAddressManuallyViewModel(formWithReplacedErrors(invalidForm), vehicleDetails.get),
      postcode)
    )
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        switch(request,
        { privateKeeperDetails => handleInvalidForm(
            invalidForm,
            privateKeeperDetails.postcode
          )
        },
        { businessKeeperDetails => handleInvalidForm(
            invalidForm,
            businessKeeperDetails.postcode
          )
        },
        message => neither(message)
        ),
      validForm =>
        switch(request,
        { privateKeeperDetails => {
          val keeperAddress = AddressModel.from(
            validForm.addressAndPostcodeModel,
            privateKeeperDetails.postcode
          )

          val keeperDetailsModel = NewKeeperDetailsViewModel(
            name = s"${privateKeeperDetails.firstName} ${privateKeeperDetails.firstName}",
            address = keeperAddress,
            email = privateKeeperDetails.email,
            isBusinessKeeper = false
          )
          // Redirect to the next screen in the workflow
          Redirect(routes.CompleteAndConfirm.present()).
            withCookie(validForm).
            withCookie(keeperDetailsModel)
        }
        },
        { businessKeeperDetails => {
          val keeperAddress = AddressModel.from(
            validForm.addressAndPostcodeModel,
            businessKeeperDetails.postcode
          )

          val keeperDetailsModel = NewKeeperDetailsViewModel(
            name = businessKeeperDetails.businessName,
            address = keeperAddress,
            email = businessKeeperDetails.email,
            fleetNumber = businessKeeperDetails.fleetNumber,
            isBusinessKeeper = true
          )
          // Redirect to the next screen in the workflow
          Redirect(routes.CompleteAndConfirm.present()).
            withCookie(validForm).
            withCookie(keeperDetailsModel)
        }
        },
        message => neither(message)
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