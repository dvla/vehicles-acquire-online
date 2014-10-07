package controllers

import javax.inject.Inject
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.mvc.{Action, Controller, Request}
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.BusinessKeeperDetailsFormModel
import models.NewKeeperChooseYourAddressFormModel
import models.NewKeeperDetailsViewModel
import models.PrivateKeeperDetailsFormModel
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.clientsidesession.{ClientSideSession, ClientSideSessionFactory}
import common.webserviceclients.addresslookup.AddressLookupService
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.new_keeper_choose_your_address

class NewKeeperChooseYourAddress @Inject()(addressLookupService: AddressLookupService)
                                         (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends Controller {

  private[controllers] val form = Form(NewKeeperChooseYourAddressFormModel.Form.Mapping)

  def present = Action.async { implicit request => request.cookies.getModel[PrivateKeeperDetailsFormModel] match {
      case Some(privateKeeperDetails) =>
        val session = clientSideSessionFactory.getSession(request.cookies)
        fetchPrivateKeeperAddresses(privateKeeperDetails)(session, request2lang).map { addresses =>
          Ok(views.html.acquire.new_keeper_choose_your_address(
            form.fill(),
            privateKeeperDetails.firstName + " " + privateKeeperDetails.lastName,
            privateKeeperDetails.postcode,
            privateKeeperDetails.email.getOrElse("Not entered"),
            addresses))
        }
      case None => request.cookies.getModel[BusinessKeeperDetailsFormModel] match {
          case Some(businessKeeperDetails) =>
            val session = clientSideSessionFactory.getSession(request.cookies)
            fetchBusinessKeeperAddresses(businessKeeperDetails)(session, request2lang).map { addresses =>
              Ok(views.html.acquire.new_keeper_choose_your_address(
                form.fill(),
                businessKeeperDetails.businessName,
                businessKeeperDetails.postcode,
                businessKeeperDetails.email.getOrElse("Not entered"),
                addresses))
            }
          case None => Future {
            Logger.error("Failed to find keeper details in cache. Now redirecting to vehicle lookup.")
            Redirect(routes.VehicleLookup.present())
          }
        }
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => request.cookies.getModel[PrivateKeeperDetailsFormModel] match {
          case Some(privateKeeperDetails) =>
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            fetchPrivateKeeperAddresses(privateKeeperDetails).map { addresses =>
              BadRequest(new_keeper_choose_your_address(formWithReplacedErrors(invalidForm),
                privateKeeperDetails.firstName + " " + privateKeeperDetails.lastName,
                privateKeeperDetails.postcode,
                privateKeeperDetails.email.getOrElse("Not entered"),
                addresses))
            }
          case None => request.cookies.getModel[BusinessKeeperDetailsFormModel] match {
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
                Logger.error("Failed to find keeper details in cache. Now redirecting to vehicle lookup.")
                Redirect(routes.VehicleLookup.present())
              }
            }
        },
      validForm =>
        request.cookies.getModel[PrivateKeeperDetailsFormModel] match {
          case Some(privateKeeperDetails) =>
            println("Private keeper details form model match, looking up uprn")
            implicit val session = clientSideSessionFactory.getSession(request.cookies)
            lookupUprn(validForm, privateKeeperDetails.firstName + " " + privateKeeperDetails.lastName, privateKeeper = true)
          case None => request.cookies.getModel[BusinessKeeperDetailsFormModel] match {
              case Some(businessKeeperDetails) =>
                implicit val session = clientSideSessionFactory.getSession(request.cookies)
                lookupUprn(validForm, businessKeeperDetails.businessName, privateKeeper = false)
              case None => Future {
                Logger.error("Failed to find new keeper details, redirecting")
                Redirect(routes.VehicleLookup.present())
              }
            }
        }
    )
  }

  def back = Action { implicit request =>
    val privateKeeperDetailsOpt = request.cookies.getModel[PrivateKeeperDetailsFormModel]
    val businessKeeperDetailsOpt = request.cookies.getModel[BusinessKeeperDetailsFormModel]
    (privateKeeperDetailsOpt, businessKeeperDetailsOpt) match {
      case (Some(privateKeeperDetails), _) =>
        Redirect(routes.PrivateKeeperDetails.present())
      case (_, Some(businessKeeperDetails)) =>
        Redirect(routes.BusinessKeeperDetails.present())
      case _ =>
        // This should never happen because this case is guarded in the present method
        Logger.warn("Failed to find a cookie for the new keeper. Now redirecting to vehicle lookup.")
        Redirect(routes.VehicleLookup.present())
    }
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
          val newKeeperDetailsModel = NewKeeperDetailsViewModel(newKeeperName = newKeeperName, newKeeperAddress = addressViewModel)
          Redirect(routes.CompleteAndConfirm.present()).withCookie(model).withCookie(newKeeperDetailsModel)
      case None => Redirect(routes.UprnNotFound.present())
    }
  }
}