package controllers

import javax.inject.Inject
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.mvc.{Action, Controller, Request}
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.{BusinessChooseYourAddressFormModel, SetupTradeDetailsFormModel}
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.clientsidesession.{ClientSideSession, ClientSideSessionFactory}
import common.model.TraderDetailsModel
import common.webserviceclients.addresslookup.AddressLookupService
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.business_choose_your_address

class BusinessChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                         (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends Controller {

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
            Logger.warn("Failed to find dealer details, redirecting")
            Redirect(routes.SetUpTradeDetails.present())
          }
        },
      validForm =>
        request.cookies.getModel[SetupTradeDetailsFormModel] match {
          case Some(setupTradeDetailsModel) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            lookupUprn(validForm, setupTradeDetailsModel.traderBusinessName, setupTradeDetailsModel.traderEmail)
          case None => Future {
            Logger.warn("Failed to find dealer details, redirecting")
            Redirect(routes.SetUpTradeDetails.present())
          }
        }
    )
  }

  private def fetchAddresses(model: SetupTradeDetailsFormModel)(implicit session: ClientSideSession, lang: Lang) =
    addressLookupService.fetchAddressesForPostcode(model.traderPostcode, session.trackingId)

  private def formWithReplacedErrors(form: Form[BusinessChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(key = AddressSelectId, message = "disposal_businessChooseYourAddress.address.required", args = Seq.empty)).
      distinctErrors

  private def lookupUprn(model: BusinessChooseYourAddressFormModel, traderName: String, traderEmail: Option[String])
                        (implicit request: Request[_], session: ClientSideSession) = {
    val lookedUpAddress = addressLookupService.fetchAddressForUprn(model.uprnSelected.toString, session.trackingId)
    lookedUpAddress.map {
      case Some(addressViewModel) =>
        val traderDetailsModel = TraderDetailsModel(traderName = traderName, traderAddress = addressViewModel, traderEmail = traderEmail)
        Redirect(routes.VehicleLookup.present()).
          discardingCookie(EnterAddressManuallyCacheKey).
          withCookie(model).
          withCookie(traderDetailsModel)
      case None => Redirect(routes.UprnNotFound.present())
    }
  }
}