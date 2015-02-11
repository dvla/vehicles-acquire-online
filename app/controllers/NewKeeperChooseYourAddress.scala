package controllers

import javax.inject.Inject
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.NewKeeperChooseYourAddressViewModel
import uk.gov.dvla.vehicles.presentation.common.model.BusinessKeeperDetailsFormModel
import uk.gov.dvla.vehicles.presentation.common.model.NewKeeperChooseYourAddressFormModel
import models.CookiePrefix
import models.NewKeeperDetailsViewModel
import models.NewKeeperEnterAddressManuallyFormModel.NewKeeperEnterAddressManuallyCacheKey
import models.NewKeeperDetailsViewModel.{createNewKeeper, getTitle}
import models.PrivateKeeperDetailsFormModel
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContent, Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.clientsidesession.ClientSideSessionFactory
import common.model.VehicleAndKeeperDetailsModel
import common.webserviceclients.addresslookup.AddressLookupService
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.new_keeper_choose_your_address

class NewKeeperChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config) extends Controller {

  private[controllers] val form = Form(NewKeeperChooseYourAddressFormModel.Form.Mapping)

  private final val KeeperDetailsNotInCacheMessage = "Failed to find keeper details in cache. " +
    "Now redirecting to vehicle lookup."
  private final val PrivateAndBusinessKeeperDetailsBothInCacheMessage = "Both private and business keeper details " +
    "found in cache. This is an error condition. Now redirecting to vehicle lookup."
  private final val VehicleDetailsNotInCacheMessage = "Failed to find vehicle details in cache. " +
    "Now redirecting to vehicle lookup"

  private def switch[R](onPrivate: PrivateKeeperDetailsFormModel => R,
                        onBusiness: BusinessKeeperDetailsFormModel => R,
                        onError: String => R)
                       (implicit request: Request[AnyContent]): R = {
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
    Logger.warn(message)
    Redirect(routes.VehicleLookup.present())
  }

  def present = Action.async { implicit request => switch(
    privateKeeperDetails => fetchAddresses(privateKeeperDetails.postcode).map { addresses =>
      if (config.ordnanceSurveyUseUprn) openView(
        constructPrivateKeeperName(privateKeeperDetails),
        privateKeeperDetails.postcode,
        privateKeeperDetails.email,
        addresses
      ) else openView(
        constructPrivateKeeperName(privateKeeperDetails),
        privateKeeperDetails.postcode,
        privateKeeperDetails.email,
        index(addresses)
      )
    },
    businessKeeperDetails => fetchAddresses(businessKeeperDetails.postcode).map { addresses =>
        if (config.ordnanceSurveyUseUprn) openView(
          businessKeeperDetails.businessName,
          businessKeeperDetails.postcode,
          businessKeeperDetails.email,
          addresses
        ) else openView(
          businessKeeperDetails.businessName,
          businessKeeperDetails.postcode,
          businessKeeperDetails.email,
          index(addresses)
        )
      },
    message => Future.successful(error(message))
  )}

  private def openView(name: String, postcode: String, email: Option[String], addresses: Seq[(String, String)])
                      (implicit request: Request[_]) = {
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(vehicleAndKeeperDetails) =>
        Ok(views.html.acquire.new_keeper_choose_your_address(
          NewKeeperChooseYourAddressViewModel(form.fill(), vehicleAndKeeperDetails),
          name,
          postcode,
          email,
          addresses
        ))
      case _ => error(VehicleDetailsNotInCacheMessage)
    }
  }

  private def handleInvalidForm(name: String, postcode: String, email: Option[String], addresses: Seq[(String, String)])
                               (implicit invalidForm: Form[NewKeeperChooseYourAddressFormModel], request: Request[_]) = {
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(vehicleAndKeeperDetails) =>
        BadRequest(new_keeper_choose_your_address(
          NewKeeperChooseYourAddressViewModel(formWithReplacedErrors(invalidForm), vehicleAndKeeperDetails),
          name,
          postcode,
          email,
          addresses
        ))
      case _ => error(VehicleDetailsNotInCacheMessage)
    }
  }

  def submit = Action.async { implicit request =>
    def onInvalidForm(implicit invalidForm: Form[NewKeeperChooseYourAddressFormModel]) = switch(
      privateKeeperDetails => fetchAddresses(privateKeeperDetails.postcode).map { addresses =>
        if (config.ordnanceSurveyUseUprn) handleInvalidForm(
          constructPrivateKeeperName(privateKeeperDetails),
          privateKeeperDetails.postcode,
          privateKeeperDetails.email,
          addresses
        ) else handleInvalidForm(
          constructPrivateKeeperName(privateKeeperDetails),
          privateKeeperDetails.postcode,
          privateKeeperDetails.email,
          index(addresses)
        )
      },
      businessKeeperDetails => fetchAddresses(businessKeeperDetails.postcode).map { addresses =>
        if (config.ordnanceSurveyUseUprn) handleInvalidForm(
          businessKeeperDetails.businessName,
          businessKeeperDetails.postcode,
          businessKeeperDetails.email,
          addresses
        ) else handleInvalidForm(
          businessKeeperDetails.businessName,
          businessKeeperDetails.postcode,
          businessKeeperDetails.email,
          index(addresses)
        )
      },
      message => Future.successful(error(message))
    )

    def onValidForm(implicit validModel: NewKeeperChooseYourAddressFormModel) = switch(
      privateKeeperDetails =>
        if (config.ordnanceSurveyUseUprn) lookupUprn(
          constructPrivateKeeperName(privateKeeperDetails),
          privateKeeperDetails.email,
          None,
          isBusinessKeeper = false
        ) else lookupAddressByPostcodeThenIndex(validModel, privateKeeperDetails.postcode),
      businessKeeperDetails =>
        if (config.ordnanceSurveyUseUprn) lookupUprn(
          businessKeeperDetails.businessName,
          businessKeeperDetails.email,
          businessKeeperDetails.fleetNumber,
          isBusinessKeeper = true
        ) else lookupAddressByPostcodeThenIndex(validModel, businessKeeperDetails.postcode),
      message => Future.successful(error(message))
    )

    form.bindFromRequest.fold(onInvalidForm(_), onValidForm(_))
  }

  def back = Action { implicit request =>
    switch(
      privateKeeperDetails => Redirect(routes.PrivateKeeperDetails.present()),
      businessKeeperDetails => Redirect(routes.BusinessKeeperDetails.present()),
      message => error(message)
    )
  }

  private def constructPrivateKeeperName(privateKeeperDetails: PrivateKeeperDetailsFormModel): String =
    s"${getTitle(privateKeeperDetails.title)} ${privateKeeperDetails.firstName} ${privateKeeperDetails.lastName}"

  private def fetchAddresses(postcode: String)(implicit request: Request[_]) = {
    val session = clientSideSessionFactory.getSession(request.cookies)
    addressLookupService.fetchAddressesForPostcode(postcode, session.trackingId)
  }

  private def index(addresses: Seq[(String, String)]) = {
    addresses.map { case (uprn, address) => address}. // Extract the address.
      zipWithIndex. // Add an index for each address
      map { case (address, index) => (index.toString, address)} // Flip them around so index comes first.
  }

  private def formWithReplacedErrors(form: Form[NewKeeperChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(key = AddressSelectId, message = "disposal_newKeeperChooseYourAddress.address.required", args = Seq.empty))
      .distinctErrors

  private def lookupUprn(newKeeperName: String,
                         email: Option[String],
                         fleetNumber: Option[String],
                         isBusinessKeeper: Boolean)
                        (implicit model: NewKeeperChooseYourAddressFormModel, request: Request[_]) = {
    val session = clientSideSessionFactory.getSession(request.cookies)
    val lookedUpAddress = addressLookupService.fetchAddressForUprn(model.uprnSelected.toString, session.trackingId)
    lookedUpAddress.map {
      case Some(addressViewModel) =>
          createNewKeeper(addressViewModel) match {
          case Some(newKeeperDetails) =>
            Redirect(routes.VehicleTaxOrSorn.present())
              .discardingCookie(NewKeeperEnterAddressManuallyCacheKey)
              .withCookie(model)
              .withCookie(newKeeperDetails)
          case _ => error("No new keeper details found in cache, redirecting to vehicle lookup")
        }
      case None => Redirect(routes.UprnNotFound.present())
    }
  }

  private def lookupAddressByPostcodeThenIndex(model: NewKeeperChooseYourAddressFormModel,
                                               postCode: String)
                                              (implicit request: Request[_]): Future[Result] = {
    fetchAddresses(postCode)(request).map { addresses =>
      val indexSelected = model.uprnSelected.toInt

      if (indexSelected < addresses.length) {
        val lookedUpAddresses = index(addresses)
        val lookedUpAddress = lookedUpAddresses(indexSelected) match {
          case (index, address) => address
        }
        val addressModel = AddressModel.from(lookedUpAddress)
        createNewKeeper(addressModel) match {
          case Some(newKeeperDetails) => nextPage(model, newKeeperDetails, addressModel)
          case _ => error("No new keeper details found in cache, redirecting to vehicle lookup")
        }
      }
      else {
        // Guard against IndexOutOfBoundsException
        Redirect(routes.UprnNotFound.present())
      }
    }
  }

  private def nextPage(newKeeperDetailsChooseYourAddressModel: NewKeeperChooseYourAddressFormModel,
                       newKeeperDetailsmodel: NewKeeperDetailsViewModel,
                       addressModel: AddressModel)
                      (implicit request: Request[_]): Result = {
    /* The redirect is done as the final step within the map so that:
     1) we are not blocking threads
     2) the browser does not change page before the future has completed and written to the cache. */
    Redirect(routes.VehicleTaxOrSorn.present()).
      discardingCookie(NewKeeperEnterAddressManuallyCacheKey).
      withCookie(newKeeperDetailsmodel).
      withCookie(newKeeperDetailsChooseYourAddressModel)
  }

}
