package controllers

import javax.inject.Inject
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.mvc.{Action, Controller, Request}
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.clientsidesession.{ClientSideSession, ClientSideSessionFactory}
import common.webserviceclients.addresslookup.AddressLookupService
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.new_keeper_choose_your_address
import scala.Some

class NewKeeperChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                         (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends Controller {

  private[controllers] val form = Form(NewKeeperChooseYourAddressFormModel.Form.Mapping)

  def present = Action.async { implicit request =>
    request.cookies.getModel[PrivateKeeperDetailsFormModel] match {
      case Some(privateKeeperDetails) =>
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchPrivateKeeperAddresses(privateKeeperDetails)(session, request2lang).map { addresses =>
          Ok(views.html.acquire.new_keeper_choose_your_address(form.fill(),
            privateKeeperDetails.firstName + " " + privateKeeperDetails.lastName,
            privateKeeperDetails.postcode,
            privateKeeperDetails.email.getOrElse("Not entered"),
            addresses))
        }
      case None =>
        request.cookies.getModel[BusinessKeeperDetailsFormModel] match {
          case Some(businessKeeperDetails) =>
            val session = clientSideSessionFactory.getSession(request.cookies)
            fetchBusinessKeeperAddresses(businessKeeperDetails)(session, request2lang).map { addresses =>
              Ok(views.html.acquire.new_keeper_choose_your_address(form.fill(),
                businessKeeperDetails.businessName,
                businessKeeperDetails.postcode,
                businessKeeperDetails.email.getOrElse("Not entered"),
                addresses))
            }
          case None => Future {
            Redirect(routes.SetUpTradeDetails.present())
          }
        }
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        request.cookies.getModel[PrivateKeeperDetailsFormModel] match {
          case Some(privateKeeperDetails) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            fetchPrivateKeeperAddresses(privateKeeperDetails).map { addresses =>
              BadRequest(new_keeper_choose_your_address(formWithReplacedErrors(invalidForm),
                privateKeeperDetails.firstName,
                privateKeeperDetails.postcode,
                privateKeeperDetails.email.getOrElse("Not entered"),
                addresses))
            }
          case None =>
            request.cookies.getModel[BusinessKeeperDetailsFormModel] match {
              case Some(businessKeeperDetails) =>
                implicit val session = clientSideSessionFactory.getSession(request.cookies)
                fetchBusinessKeeperAddresses(businessKeeperDetails).map { addresses =>
                  BadRequest(new_keeper_choose_your_address(formWithReplacedErrors(invalidForm),
                    businessKeeperDetails.businessName,
                    businessKeeperDetails.postcode,
                    businessKeeperDetails.email.getOrElse("Not entered"),
                    addresses))
                }
              case None => Future {
                Logger.error("Failed to find new keeper details, redirecting")
                Redirect(routes.SetUpTradeDetails.present())
              }
            }
        },
      validForm =>
        request.cookies.getModel[PrivateKeeperDetailsFormModel] match {
          case Some(privateKeeperDetails) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            lookupUprn(validForm, privateKeeperDetails.firstName + " " + privateKeeperDetails.lastName, true)
          case None =>
            request.cookies.getModel[BusinessKeeperDetailsFormModel] match {
              case Some(businessKeeperDetails) =>
                implicit val session = clientSideSessionFactory.getSession(request.cookies)
                lookupUprn(validForm, businessKeeperDetails.businessName, false)
              case None => Future {
                Logger.error("Failed to find new keeper details, redirecting")
                Redirect(routes.SetUpTradeDetails.present())
              }
            }
        }
    )
  }

  private def fetchPrivateKeeperAddresses(model: PrivateKeeperDetailsFormModel)(implicit session: ClientSideSession, lang: Lang) =
    addressLookupService.fetchAddressesForPostcode(model.postcode, session.trackingId)

  private def fetchBusinessKeeperAddresses(model: BusinessKeeperDetailsFormModel)(implicit session: ClientSideSession, lang: Lang) =
    addressLookupService.fetchAddressesForPostcode(model.postcode, session.trackingId)

  private def formWithReplacedErrors(form: Form[NewKeeperChooseYourAddressFormModel])(implicit request: Request[_]) =
    form.replaceError(AddressSelectId, "error.required",
      FormError(key = AddressSelectId, message = "disposal_newKeeperChooseYourAddress.address.required", args = Seq.empty)).
      distinctErrors

  private def lookupUprn(model: NewKeeperChooseYourAddressFormModel, newKeeperName: String, privateKeeper: Boolean)
                        (implicit request: Request[_], session: ClientSideSession) = {
    val lookedUpAddress = addressLookupService.fetchAddressForUprn(model.uprnSelected.toString, session.trackingId)
    lookedUpAddress.map {
      case Some(addressViewModel) =>
        if (privateKeeper) {
          val newKeeperDetailsModel = NewKeeperDetailsModel(newKeeperName = newKeeperName, newKeeperAddress = addressViewModel)
          Redirect(routes.PrivateKeeperDetailsComplete.present()).withCookie(model).withCookie(newKeeperDetailsModel)
        } else {
          val newKeeperDetailsModel = NewKeeperDetailsModel(newKeeperName = newKeeperName, newKeeperAddress = addressViewModel)
          Redirect(routes.BusinessKeeperDetailsComplete.present()).
            withCookie(model).
            withCookie(newKeeperDetailsModel)
        }
      case None => Redirect(routes.UprnNotFound.present())
    }
  }
}