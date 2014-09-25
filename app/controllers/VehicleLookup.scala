package controllers

import com.google.inject.Inject
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import models.{VehicleLookupFormModel, VehicleLookupViewModel}
import play.api.data.{Form, FormError}
import play.api.mvc.{Call, Action, Request}
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupBase.{VehicleFound, VehicleNotFound, LookupResult}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.model.{VehicleDetailsModel, TraderDetailsModel}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.vehiclelookup.VehicleDetailsRequestDto
import common.webserviceclients.vehiclelookup.VehicleDetailsResponseDto
import common.webserviceclients.vehiclelookup.VehicleDetailsDto
import common.webserviceclients.vehiclelookup.VehicleLookupService
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import utils.helpers.Config
import views.acquire.VehicleLookup.VehicleSoldTo_Private

final class VehicleLookup @Inject()(val bruteForceService: BruteForcePreventionService,
                                    vehicleLookupService: VehicleLookupService,
                                    dateService: DateService)
                                   (implicit val clientSideSessionFactory: ClientSideSessionFactory,
                                    config: Config) extends VehicleLookupBase {

  override val vrmLocked: Call = routes.VrmLocked.present()
  override val microServiceError: Call = routes.MicroServiceError.present()
  override val vehicleLookupFailure: Call = routes.VehicleLookupFailure.present()
  override val responseCodeCacheKey: String = VehicleLookupResponseCodeCacheKey

  override type Form = VehicleLookupFormModel

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
        bruteForceAndLookup(
          validForm.registrationNumber,
          validForm.referenceNumber,
          validForm)
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

  override protected def callLookupService(trackingId: String, form: Form)(implicit request: Request[_]): Future[LookupResult] = {
    val vehicleDetailsRequest = VehicleDetailsRequestDto(
      referenceNumber = form.referenceNumber,
      registrationNumber = form.registrationNumber,
      userName = request.cookies.getModel[TraderDetailsModel].fold("")(_.traderName)
    )

    vehicleLookupService.invoke(vehicleDetailsRequest, trackingId) map {
      case (responseStatusVehicleLookupMS: Int, vehicleDetailsResponse: Option[VehicleDetailsResponseDto]) =>
        responseStatusVehicleLookupMS match {
          case OK =>
            vehicleDetailsResponse match {
              case Some(response) =>
                response.responseCode match {
                  case Some(responseCode) => VehicleNotFound(responseCode)
                  case None =>
                    response.vehicleDetailsDto match {
                      case Some(dto) => VehicleFound(vehicleFoundResult(dto, form.vehicleSoldTo))
                      case None => throw new RuntimeException("No vehicleDetailsDto found")
                    }
                }
              case _ => throw new RuntimeException("No vehicleDetailsResponse found")
            }
          case faultCode => throw new RuntimeException(
            s"Vehicle lookup web service call http status not OK, it " +
              s"was: $faultCode. Problem may come from either vehicle lookup micro-service or the VSS"
          )
        }
    }
  }

  private def vehicleFoundResult(vehicleDetailsDto: VehicleDetailsDto, soldTo: String)(implicit request: Request[_]) =
    VehicleDetailsModel.fromDto(vehicleDetailsDto).disposeFlag match {
      case true => vehicleDisposedResult(vehicleDetailsDto, soldTo)
      case false =>
        Redirect(routes.KeeperStillOnRecord.present()).
          withCookie(VehicleDetailsModel.fromDto(vehicleDetailsDto))
    }

  private def vehicleDisposedResult(vehicleDetailsDto: VehicleDetailsDto, soldTo: String)(implicit request: Request[_]) =
    soldTo match {
      case VehicleSoldTo_Private =>
        Redirect(routes.PrivateKeeperDetails.present()).
          withCookie(VehicleDetailsModel.fromDto(vehicleDetailsDto))
      case _ =>
        Redirect(routes.BusinessKeeperDetails.present()).
          withCookie(VehicleDetailsModel.fromDto(vehicleDetailsDto))
    }
}
