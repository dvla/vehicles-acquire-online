package controllers

import javax.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.mvc.{Action, Controller, Request, Result}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.clientsidesession.{ClientSideSession, ClientSideSessionFactory}
import common.LogFormats.DVLALogger
import common.model.{AddressModel, SetupTradeDetailsFormModel, TraderDetailsModel, VmAddressModel}
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.addresslookup.AddressLookupService
import utils.helpers.Config
import views.html.acquire.business_choose_your_address

class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                         (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends Controller with DVLALogger {

  private[controllers] val form = Form(BusinessChooseYourAddressFormModel.Form.Mapping)

  def present = Action.async { implicit request =>
    request.cookies.getModel[SetupTradeDetailsFormModel] match {
      case Some(setupTradeDetailsModel) =>
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchAddresses(setupTradeDetailsModel)(session, request2lang).map { addresses =>
          Ok(views.html.acquire.business_choose_your_address(form.fill(),
            setupTradeDetailsModel.traderBusinessName,
            setupTradeDetailsModel.traderPostcode,
            setupTradeDetailsModel.traderEmail,
            addresses))
        }
      case None => Future {
        logMessage(request.cookies.trackingId(), Warn,
            s"Failed to find dealer details, redirecting to ${routes.SetUpTradeDetails.present()}")
        Redirect(routes.SetUpTradeDetails.present())
      }
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetails) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            fetchAddresses(setupTradeDetails).map { addresses =>
                BadRequest(business_choose_your_address(formWithReplacedErrors(invalidForm),
                    setupTradeDetails.traderBusinessName,
                    setupTradeDetails.traderPostcode,
                    setupTradeDetails.traderEmail,
                    addresses))
            }
          case None => Future {
            logMessage(request.cookies.trackingId(), Warn,
                s"Failed to find dealer details, redirecting to ${routes.SetUpTradeDetails.present()}")

            Redirect(routes.SetUpTradeDetails.present())
          }
        },
      validForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetailsModel) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            lookupAddressByPostcodeThenIndex(validForm, setupTradeDetailsModel)
          case None => Future {
            logMessage(request.cookies.trackingId(), Warn,
              s"Failed to find dealer details, redirecting to ${routes.SetUpTradeDetails.present()}")
            Redirect(routes.SetUpTradeDetails.present())
          }
        }
    )
  }

  private def index(addresses: Seq[(String, String)]) = {
    addresses.map { case (uprn, address) => address}. // Extract the address.
      zipWithIndex. // Add an index for each address
      map { case (address, index) => (index.toString, address)} // Flip them around so index comes first.
  }

  private def lookupAddressByPostcodeThenIndex(model: BusinessChooseYourAddressFormModel,
                                               setupBusinessDetailsForm: SetupTradeDetailsFormModel)
                                              (implicit request: Request[_], session: ClientSideSession): Future[Result] = {
    fetchAddresses(setupBusinessDetailsForm)(session, request2lang).map { addresses =>
        val lookedUpAddress = model.uprnSelected
        val addressModel = VmAddressModel.from(lookedUpAddress)
        nextPage(model, setupBusinessDetailsForm.traderBusinessName, addressModel, setupBusinessDetailsForm.traderEmail)
    }
  }

  private def fetchAddresses(model: SetupTradeDetailsFormModel)
                            (implicit session: ClientSideSession, lang: Lang): Future[Seq[(String, String)]] =
    addressLookupService.fetchAddressesForPostcode(
      model.traderPostcode,
      session.trackingId)

  private def formWithReplacedErrors(form: Form[BusinessChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(
        key = AddressSelectId,
        message = "disposal_businessChooseYourAddress.address.required",
        args = Seq.empty)
      ).distinctErrors

  private def nextPage(model: BusinessChooseYourAddressFormModel,
                       traderName: String,
                       addressModel: AddressModel,
                       traderEmail: Option[String])
                      (implicit request: Request[_], session: ClientSideSession): Result = {
    val traderDetailsModel = TraderDetailsModel(
      traderName = traderName,
      traderAddress = addressModel,
      traderEmail = traderEmail
    )
    logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.VehicleLookup.present()}")
    Redirect(routes.VehicleLookup.present())
      .discardingCookie(EnterAddressManuallyCacheKey)
      .withCookie(model)
      .withCookie(traderDetailsModel)
  }
}
