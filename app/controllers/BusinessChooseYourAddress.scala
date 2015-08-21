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
        fetchAddresses(setupTradeDetailsModel, showBusinessName = Some(true))(session, request2lang).map { addresses =>
          if (config.ordnanceSurveyUseUprn) Ok(views.html.acquire.business_choose_your_address(form.fill(),
            setupTradeDetailsModel.traderBusinessName,
            setupTradeDetailsModel.traderPostcode,
            setupTradeDetailsModel.traderEmail,
            addresses))
          else Ok(views.html.acquire.business_choose_your_address(form.fill(),
            setupTradeDetailsModel.traderBusinessName,
            setupTradeDetailsModel.traderPostcode,
            setupTradeDetailsModel.traderEmail,
            index(addresses)))
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
            fetchAddresses(setupTradeDetails, showBusinessName = Some(true)).map { addresses =>
              if (config.ordnanceSurveyUseUprn) {
                BadRequest(business_choose_your_address(formWithReplacedErrors(invalidForm),
                    setupTradeDetails.traderBusinessName,
                    setupTradeDetails.traderPostcode,
                    setupTradeDetails.traderEmail,
                    addresses))
              } else {
                BadRequest(business_choose_your_address(formWithReplacedErrors(invalidForm),
                  setupTradeDetails.traderBusinessName,
                  setupTradeDetails.traderPostcode,
                  setupTradeDetails.traderEmail,
                  index(addresses)))
              }
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
            if (config.ordnanceSurveyUseUprn)
              lookupUprn(validForm, setupTradeDetailsModel.traderBusinessName, setupTradeDetailsModel.traderEmail)
            else
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
    fetchAddresses(setupBusinessDetailsForm, showBusinessName = Some(false))(session, request2lang).map { addresses =>
      val indexSelected = model.uprnSelected.toInt
      if (indexSelected < addresses.length) {
        val lookedUpAddresses = index(addresses)
        val lookedUpAddress = lookedUpAddresses(indexSelected) match {
          case (index, address) => address
        }
        val addressModel = VmAddressModel.from(lookedUpAddress)
        nextPage(model, setupBusinessDetailsForm.traderBusinessName, addressModel, setupBusinessDetailsForm.traderEmail)
      }
      else {
        // Guard against IndexOutOfBoundsException
        logMessage(request.cookies.trackingId(), Warn,
          s"Failed to find address details, redirecting to ${routes.UprnNotFound.present()}")
        Redirect(routes.UprnNotFound.present())
      }
    }
  }

  private def fetchAddresses(model: SetupTradeDetailsFormModel, showBusinessName: Option[Boolean])
                            (implicit session: ClientSideSession, lang: Lang): Future[Seq[(String, String)]] =
    addressLookupService.fetchAddressesForPostcode(
      model.traderPostcode,
      session.trackingId,
      showBusinessName = showBusinessName
    )

  private def formWithReplacedErrors(form: Form[BusinessChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(
        key = AddressSelectId,
        message = "disposal_businessChooseYourAddress.address.required",
        args = Seq.empty)
      ).distinctErrors

  private def lookupUprn(model: BusinessChooseYourAddressFormModel, traderName: String, traderEmail: Option[String])
                        (implicit request: Request[_], session: ClientSideSession) = {
    val lookedUpAddress = addressLookupService.fetchAddressForUprn(model.uprnSelected.toString, session.trackingId)
    lookedUpAddress.map {
      case Some(addressViewModel) =>
        nextPage(model, traderName, addressViewModel, traderEmail)
      case None =>
        logMessage(request.cookies.trackingId(), Warn,
            s"Failed to find address details, redirecting to ${routes.UprnNotFound.present()}")
        Redirect(routes.UprnNotFound.present())
    }
  }

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
