package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.LogFormats
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.model.{VehicleDetailsModel, TraderDetailsModel}
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.vehiclelookup.{VehicleDetailsRequestDto, VehicleDetailsResponseDto, VehicleDetailsDto, VehicleLookupService}
import utils.helpers.Config
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import models.{VehicleLookupFormModel, VehicleLookupViewModel}
import views.acquire.VehicleLookup.VehicleSoldTo_Private
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleLookup @Inject()(vehicleLookupService: VehicleLookupService)
                                   (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends Controller {

  private[controllers] val form = Form(
    VehicleLookupFormModel.Form.Mapping
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
                VehicleLookupFormModel.Form.VehicleRegistrationNumberId,
                FormError(
                  key = VehicleLookupFormModel.Form.VehicleRegistrationNumberId,
                  message = "error.restricted.validVrnOnly",
                  args = Seq.empty
                )
              ).replaceError(
                  VehicleLookupFormModel.Form.DocumentReferenceNumberId,
                  FormError(
                    key = VehicleLookupFormModel.Form.DocumentReferenceNumberId,
                    message = "error.validDocumentReferenceNumber",
                    args = Seq.empty)
                ).distinctErrors

              BadRequest(views.html.acquire.vehicle_lookup(
                VehicleLookupViewModel(
                  formWithReplacedErrors,
                  traderDetails.traderName,
                  traderDetails.traderAddress.address
              )))
            case None => Redirect(routes.SetUpTradeDetails.present())
          }
        },
      validForm => {
        lookupVehicleResult(validForm)
      }
    )
  }

  def back = Action { implicit request =>
    request.cookies.getModel[TraderDetailsModel] match {
      case Some(dealerDetails) =>
        if (dealerDetails.traderAddress.uprn.isDefined) Redirect(routes.BusinessChooseYourAddress.present())
        else Redirect(routes.EnterAddressManually.present())
      case None => Redirect(routes.SetUpTradeDetails.present())
    }
  }

  private def lookupVehicleResult(model: VehicleLookupFormModel)
                                 (implicit request: Request[_]): Future[Result] = {

    def vehicleFoundResult(vehicleDetailsDto: VehicleDetailsDto, soldTo: String) = {
      VehicleDetailsModel.fromDto(vehicleDetailsDto).disposeFlag match {
        case true => vehicleDisposedResult(vehicleDetailsDto, soldTo)
        case false =>
          Redirect(routes.KeeperStillOnRecord.present()).
            withCookie(VehicleDetailsModel.fromDto(vehicleDetailsDto))
      }
    }

    def vehicleDisposedResult(vehicleDetailsDto: VehicleDetailsDto, soldTo: String) = {
      soldTo match {
        case VehicleSoldTo_Private =>
          Redirect(routes.PrivateKeeperDetails.present()).
            withCookie(VehicleDetailsModel.fromDto(vehicleDetailsDto))
        case _ =>
          Redirect(routes.BusinessKeeperDetails.present()).
            withCookie(VehicleDetailsModel.fromDto(vehicleDetailsDto))
      }
    }

    def vehicleNotFoundResult(responseCode: String) = {
      val enterAddressManualHtml = views.html.acquire.enter_address_manually
      val registrationNumber = LogFormats.anonymize(model.registrationNumber)
      Logger.debug(
        s"VehicleLookup encountered a problem with request $enterAddressManualHtml " +
          s"$registrationNumber, redirect to VehicleLookupFailure"
      )
      // TODO : use the correct redirect when the target exists
      Redirect(routes.NotImplemented.present())
        .withCookie(key = VehicleLookupResponseCodeCacheKey, value = responseCode)
    }

    def microServiceErrorResult(message: String) = {
      // TODO : use the correct redirect when the target exists
      Logger.error(message)
      Redirect(routes.NotImplemented.present())
    }

    def microServiceThrowableResult(message: String, t: Throwable) = {
      // TODO : use the correct redirect when the target exists
      Logger.error(message, t)
      Redirect(routes.NotImplemented.present())
    }

    def createResultFromVehicleLookupResponse(vehicleDetailsResponse: VehicleDetailsResponseDto,
                                               soldTo: String)
                                             (implicit request: Request[_]) =
      vehicleDetailsResponse.responseCode match {
        case Some(responseCode) => vehicleNotFoundResult(responseCode) // There is only a response code when there is a problem.
        case None =>
          // Happy path when there is no response code therefore no problem.
          vehicleDetailsResponse.vehicleDetailsDto match {
            case Some(dto) => vehicleFoundResult(dto, soldTo)
            case None => microServiceErrorResult(message = "No vehicleDetailsDto found")
          }
      }

    def vehicleLookupSuccessResponse(responseStatusVehicleLookupMS: Int,
                                     soldTo: String,
                                     vehicleDetailsResponse: Option[VehicleDetailsResponseDto])
                                    (implicit request: Request[_]) =
      responseStatusVehicleLookupMS match {
        case OK =>
          vehicleDetailsResponse match {
            case Some(response) => createResultFromVehicleLookupResponse(response, soldTo)
            case _ => microServiceErrorResult("No vehicleDetailsResponse found") // TODO write test to achieve code coverage.
          }
        case _ => microServiceErrorResult(s"VehicleLookup web service call http status not OK, it was: " +
          s"$responseStatusVehicleLookupMS. Problem may come from either vehicle-lookup micro-service or the VSS")
      }

    val trackingId = request.cookies.trackingId()
    val vehicleDetailsRequest = VehicleDetailsRequestDto(
      referenceNumber = model.referenceNumber,
      registrationNumber = model.registrationNumber,
      userName = request.cookies.getModel[TraderDetailsModel].fold("")(_.traderName)
    )
    vehicleLookupService.invoke(vehicleDetailsRequest, trackingId).map {
      case (responseStatusVehicleLookupMS: Int, vehicleDetailsResponse: Option[VehicleDetailsResponseDto]) =>
        vehicleLookupSuccessResponse(
          responseStatusVehicleLookupMS = responseStatusVehicleLookupMS,
          soldTo = model.vehicleSoldTo,
          vehicleDetailsResponse = vehicleDetailsResponse).
          withCookie(model)

    }.recover {
      case e: Throwable => microServiceThrowableResult(message = s"VehicleLookup Web service call failed.", e)
    }
  }

}
