package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import viewmodels.SetupTradeDetailsViewModel.Form._

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm}
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel, BruteForcePreventionModel}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehiclelookup.{VehicleLookupService, VehicleDetailsResponseDto, VehicleDetailsRequestDto, VehicleDetailsDto}
import utils.helpers.Config
import viewmodels._ //DisposeFormViewModel.{DisposeOccurredCacheKey, PreventGoingToDisposePageCacheKey, SurveyRequestTriggerDateCacheKey}
import viewmodels.{VehicleLookupViewModel, AllCacheKeys, VehicleLookupFormViewModel}
import viewmodels.VehicleLookupFormViewModel.VehicleLookupResponseCodeCacheKey

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleLookup @Inject()(vehicleLookupService: VehicleLookupService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends Controller {

  private[controllers] val form = Form(
    VehicleLookupFormViewModel.Form.Mapping
  )

  def present = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(traderDetails) =>
        Ok(views.html.acquire.vehicle_lookup(
          VehicleLookupViewModel(
            form.fill(),
            traderDetails.traderName,
            traderDetails.traderAddress.address
          )))
      case None =>
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        Future {
          request.cookies.getModel[TraderDetailsModel] match {
            case Some(traderDetails) =>
              val formWithReplacedErrors = invalidForm.replaceError(
                VehicleLookupFormViewModel.Form.VehicleRegistrationNumberId,
                FormError(
                  key = VehicleLookupFormViewModel.Form.VehicleRegistrationNumberId,
                  message = "error.restricted.validVrnOnly",
                  args = Seq.empty
                )
              ).replaceError(
                  VehicleLookupFormViewModel.Form.DocumentReferenceNumberId,
                  FormError(
                    key = VehicleLookupFormViewModel.Form.DocumentReferenceNumberId,
                    message = "error.validDocumentReferenceNumber",
                    args = Seq.empty)
                ).distinctErrors

              BadRequest(views.html.acquire.vehicle_lookup(
                VehicleLookupViewModel(
                  formWithReplacedErrors,
                  traderDetails.traderName,
                  traderDetails.traderAddress.address
              )))
            case None => Redirect(routes.NotImplemented.present()) //ToDo replace with redirect to next controller when implemented
          }
        },
      validForm => {
        Future(Redirect(routes.NotImplemented.present()))
        //Future(Ok(views.html.acquire.not_implemented)) //ToDo replace with redirect to next controller when implemented
      }
    )
  }

  def back = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) =>
        if (dealerDetails.traderAddress.uprn.isDefined) Redirect(routes.BusinessChooseYourAddress.present())
        else Redirect(routes.NotImplemented.present()) // TODO : Redirect to enter address manually
      case None => Redirect(routes.SetUpTradeDetails.present())
    }
  }

}
