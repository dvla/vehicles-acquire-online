package controllers

import com.google.inject.Inject
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common
import common.LogFormats
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.model.{BruteForcePreventionModel, VehicleDetailsModel, TraderDetailsModel}
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.vehiclelookup.VehicleDetailsRequestDto
import common.webserviceclients.vehiclelookup.VehicleDetailsResponseDto
import common.webserviceclients.vehiclelookup.VehicleDetailsDto
import common.webserviceclients.vehiclelookup.VehicleLookupService
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import models.{VehicleLookupFormModel, VehicleLookupViewModel}
import views.acquire.VehicleLookup.VehicleSoldTo_Private
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class VehicleLookup @Inject()(bruteForceService: BruteForcePreventionService,
                                    vehicleLookupService: VehicleLookupService,
                                    dateService: DateService)
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
        bruteForceAndLookup(validForm)
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

  private def bruteForceAndLookup(formModel: VehicleLookupFormModel)
                                 (implicit request: Request[_]): Future[Result] =

    bruteForceService.isVrmLookupPermitted(formModel.registrationNumber).flatMap { bruteForcePreventionViewModel =>
      // US270: The security micro-service will return a Forbidden (403) message when the vrm is locked, we have hidden that logic as a boolean.
      if (bruteForcePreventionViewModel.permitted) lookupVehicleResult(formModel, bruteForcePreventionViewModel)
      else Future.successful {
        val registrationNumber = LogFormats.anonymize(formModel.registrationNumber)
        Logger.warn(s"BruteForceService locked out vrm: $registrationNumber")
        Redirect(routes.VrmLocked.present()).
          withCookie(bruteForcePreventionViewModel)
      }
    } recover {
      case exception: Throwable =>
        Logger.error(
          s"Exception thrown by BruteForceService so for safety we won't let anyone through. " +
            s"Exception ${exception.getStackTraceString}"
        )
        Redirect(routes.MicroServiceError.present())
    }


  private def lookupVehicleResult(model: VehicleLookupFormModel,
                                  bruteForcePreventionViewModel: BruteForcePreventionModel)
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
      val registrationNumber = LogFormats.anonymize(model.registrationNumber)
      Logger.debug(
        s"VehicleLookup did not find vehicle with vrm " +
          s"$registrationNumber. Received response code $responseCode, redirecting to VehicleLookupFailure"
      )
      Redirect(routes.VehicleLookupFailure.present())
        .withCookie(key = VehicleLookupResponseCodeCacheKey, value = responseCode)
    }

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      Redirect(routes.MicroServiceError.present())
    }

    def microServiceThrowableResult(message: String, t: Throwable) = {
      Logger.error(message, t)
      Redirect(routes.MicroServiceError.present())
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
          withCookie(model).
          withCookie(bruteForcePreventionViewModel)
    }.recover {
      case e: Throwable => microServiceThrowableResult(message = s"VehicleLookup Web service call failed.", e)
    }
  }

}
