package controllers

import com.google.inject.Inject
import models.BusinessKeeperDetailsFormModel
import models.NewKeeperDetailsViewModel
import models.NewKeeperEnterAddressManuallyFormModel
import models.PrivateKeeperDetailsFormModel
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContent, Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichForm, RichCookies, RichResult}
import common.model.AddressModel
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.new_keeper_enter_address_manually

final class NewKeeperEnterAddressManually @Inject()()
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config) extends Controller {

  private[controllers] val form = Form(
    NewKeeperEnterAddressManuallyFormModel.Form.Mapping
  )

  private final val KeeperDetailsNotInCacheMessage = "Failed to find keeper details in cache. Now redirecting to vehicle lookup."
  private final val PrivateAndBusinessKeeperDetailsBothInCacheMessage = "Both private and business keeper details found in cache. " +
    "This is an error condition. Now redirecting to vehicle lookup."

  private def switch[R](request: Request[AnyContent],
                        priv: PrivateKeeperDetailsFormModel => R,
                        business: BusinessKeeperDetailsFormModel => R,
                        neither: String => R): R = {
    val privateKeeperDetailsOpt = request.cookies.getModel[PrivateKeeperDetailsFormModel]
    val businessKeeperDetailsOpt = request.cookies.getModel[BusinessKeeperDetailsFormModel]
    (privateKeeperDetailsOpt, businessKeeperDetailsOpt) match {
      case (Some(privateKeeperDetails), Some(businessKeeperDetails)) => neither(PrivateAndBusinessKeeperDetailsBothInCacheMessage)
      case (Some(privateKeeperDetails), _) => priv(privateKeeperDetails)
      case (_, Some(businessKeeperDetails)) => business(businessKeeperDetails)
      case _ => neither(KeeperDetailsNotInCacheMessage)
    }
  }

  private def neither(message: String): Result = {
    Logger.warn(message)
    Redirect(routes.VehicleLookup.present())
  }

  def present = Action { implicit request =>
    switch(request,
      { privateKeeperDetails => Ok(new_keeper_enter_address_manually(form.fill(), privateKeeperDetails.postcode)) },
      { businessKeeperDetails => Ok(new_keeper_enter_address_manually(form.fill(), businessKeeperDetails.postcode)) },
      message => neither(message)
    )
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        switch(request,
        { privateKeeperDetails => BadRequest(
            new_keeper_enter_address_manually(formWithReplacedErrors(invalidForm),
            privateKeeperDetails.postcode)
          )
        },
        { businessKeeperDetails => BadRequest(
            new_keeper_enter_address_manually(formWithReplacedErrors(invalidForm),
            businessKeeperDetails.postcode)
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
