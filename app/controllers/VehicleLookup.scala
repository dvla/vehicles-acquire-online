package controllers

import com.google.inject.Inject
import models.{VehicleLookupFormModel, VehicleLookupViewModel}
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import models.{BusinessKeeperDetailsCacheKeys, EnterAddressManuallyFormModel, PrivateKeeperDetailsCacheKeys}
import org.joda.time.DateTime
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, Call, Request}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.controllers.VehicleLookupBase
import common.controllers.VehicleLookupBase.LookupResult
import common.controllers.VehicleLookupBase.VehicleFound
import common.controllers.VehicleLookupBase.VehicleNotFound
import common.model.{VehicleAndKeeperDetailsModel, TraderDetailsModel}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.common.{DmsWebEndUserDto, DmsWebHeaderDto}
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsDto
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsRequest
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupService
import utils.helpers.Config
import views.acquire.VehicleLookup.VehicleSoldTo_Private

class VehicleLookup @Inject()(val bruteForceService: BruteForcePreventionService,
                                    vehicleAndKeeperLookupService: VehicleAndKeeperLookupService,
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
            traderDetails.traderAddress.address,
            traderDetails.traderEmail
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
                  VehicleLookupFormModel.Form.VehicleSoldToId,
                  FormError(
                    key = VehicleLookupFormModel.Form.VehicleSoldToId,
                    message = "error.restricted.validKeeperOption",
                    args = Seq.empty)
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
                  traderDetails.traderAddress.address,
                  traderDetails.traderEmail
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
    request.cookies.getModel[EnterAddressManuallyFormModel] match {
      case Some(manualAddress) =>
        Redirect(routes.EnterAddressManually.present())
      case None => Redirect(routes.BusinessChooseYourAddress.present())
    }
  }

  override protected def callLookupService(trackingId: String, form: Form)(implicit request: Request[_]): Future[LookupResult] = {
    val vehicleAndKeeperDetailsRequest = VehicleAndKeeperDetailsRequest(
      dmsHeader = buildHeader(trackingId),
      referenceNumber = form.referenceNumber,
      registrationNumber = form.registrationNumber,
      transactionTimestamp = new DateTime
    )

    vehicleAndKeeperLookupService.invoke(vehicleAndKeeperDetailsRequest, trackingId) map { response =>
      response.responseCode match {
        case Some(responseCode) =>
          VehicleNotFound(responseCode)
        case None =>
          response.vehicleAndKeeperDetailsDto match {
            case Some(dto) => VehicleFound(vehicleFoundResult(dto, form.vehicleSoldTo))
            case None => throw new RuntimeException("No vehicleAndKeeperDetailsDto found")
          }
      }
    }
  }

  private def vehicleFoundResult(vehicleAndKeeperDetailsDto: VehicleAndKeeperDetailsDto, soldTo: String)(implicit request: Request[_]) = {
    val model = VehicleAndKeeperDetailsModel.from(vehicleAndKeeperDetailsDto)
    val disposed = model.disposeFlag.getOrElse(true)

    if (disposed) vehicleDisposedResult(model, soldTo) else Redirect(routes.KeeperStillOnRecord.present()).withCookie(model)
  }

  private def vehicleDisposedResult(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel, soldTo: String)(implicit request: Request[_]) = {
    val (call, discardedCookies) =
      if (soldTo == VehicleSoldTo_Private)
        (routes.PrivateKeeperDetails.present(), BusinessKeeperDetailsCacheKeys)
      else
        (routes.BusinessKeeperDetails.present(), PrivateKeeperDetailsCacheKeys)

    Redirect(call).
      discardingCookies(discardedCookies).
      withCookie(vehicleAndKeeperDetailsModel)
  }

  private def buildHeader(trackingId: String): DmsWebHeaderDto = {
    val alwaysLog = true
    val englishLanguage = "EN"
    DmsWebHeaderDto(conversationId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      channelCode = config.channelCode,
      contactId = config.contactId,
      eventFlag = alwaysLog,
      serviceTypeCode = config.serviceTypeCode,
      languageCode = englishLanguage,
      endUser = buildEndUser)
  }

  private def buildEndUser: DmsWebEndUserDto = {
    DmsWebEndUserDto(endUserTeamCode = config.applicationCode,
      endUserTeamDesc = config.applicationCode,
      endUserRole = config.applicationCode,
      endUserId = config.applicationCode,
      endUserIdDesc = config.applicationCode,
      endUserLongNameDesc = config.applicationCode)
  }
}