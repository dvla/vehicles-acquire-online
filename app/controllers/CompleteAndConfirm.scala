package controllers

import com.google.inject.Inject
import models.AcquireCompletionViewModel
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmViewModel
import models.NewKeeperDetailsViewModel
import models.VehicleLookupFormModel
import models.VehicleTaxOrSornFormModel
import models.CompleteAndConfirmFormModel.Form.{MileageId, ConsentId}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{FormError, Form}
import play.api.mvc.{Action, AnyContent, Call, Controller, Request, Result}
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.mappings.TitleType
import common.model.{VehicleDetailsModel, TraderDetailsModel}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.complete_and_confirm
import webserviceclients.acquire.AcquireRequestDto
import webserviceclients.acquire.AcquireResponseDto
import webserviceclients.acquire.AcquireService
import webserviceclients.acquire.KeeperDetailsDto
import webserviceclients.acquire.TitleTypeDto
import webserviceclients.acquire.TraderDetailsDto

class CompleteAndConfirm @Inject()(webService: AcquireService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                               dateService: DateService,
                                                               config: Config) extends Controller {

  private[controllers] val form = Form(
    CompleteAndConfirmFormModel.Form.Mapping
  )

  private final val NoCookiesFoundMessage = "Failed to find new keeper details and or vehicle details and or " +
    "vehicle sorn details in cache. Now redirecting to vehicle lookup"

  def present = Action { implicit request =>
    val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
    val vehicleDetailsOpt = request.cookies.getModel[VehicleDetailsModel]
    val vehicleSornOpt = request.cookies.getModel[VehicleTaxOrSornFormModel]
    (newKeeperDetailsOpt, vehicleDetailsOpt, vehicleSornOpt) match {
      case (Some(newKeeperDetails), Some(vehicleDetails), Some(vehicleSorn)) =>
        Ok(complete_and_confirm(CompleteAndConfirmViewModel(form.fill(), vehicleDetails, newKeeperDetails, vehicleSorn), dateService))
      case _ => redirectToVehicleLookup(NoCookiesFoundMessage)
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => Future.successful {
        val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
        val vehicleDetailsOpt = request.cookies.getModel[VehicleDetailsModel]
        val vehicleSornOpt = request.cookies.getModel[VehicleTaxOrSornFormModel]
        (newKeeperDetailsOpt, vehicleDetailsOpt, vehicleSornOpt) match {
          case (Some(newKeeperDetails), Some(vehicleDetails), Some(vehicleSorn)) =>
            BadRequest(complete_and_confirm(
              CompleteAndConfirmViewModel(formWithReplacedErrors(invalidForm), vehicleDetails, newKeeperDetails, vehicleSorn), dateService)
            )
          case _ =>
            Logger.error("Could not find expected data in cache on dispose submit - now redirecting...")
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => {
        val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
        val vehicleLookupOpt = request.cookies.getModel[VehicleLookupFormModel]
        val vehicleDetailsOpt = request.cookies.getModel[VehicleDetailsModel]
        val traderDetailsOpt = request.cookies.getModel[TraderDetailsModel]
        (newKeeperDetailsOpt, vehicleLookupOpt, vehicleDetailsOpt, traderDetailsOpt) match {
          case (Some(newKeeperDetails), Some(vehicleLookup), Some(vehicleDetails), Some(traderDetails)) =>
            if (config.isMicroserviceIntegrationEnabled){
            acquireAction (validForm,
                           newKeeperDetails,
                           vehicleLookup,
                           vehicleDetails,
                           traderDetails,
                           request.cookies.trackingId())}
            else {
              Future.successful {
                Logger.debug("Microservice integration is disabled")
                val transactionTimestamp = dateService.now.toDateTime

                val acquireModel = AcquireCompletionViewModel(vehicleDetails,
                  traderDetails,
                  newKeeperDetails,
                  validForm,
                  "12345",
                  transactionTimestamp)

                Redirect(routes.AcquireSuccess.present()).withCookie(acquireModel)
              }
            }
          case (_, _, _, None) => Future.successful {
            Logger.error("Could not find either dealer details or VehicleLookupFormModel in cache on Acquire submit")
            Redirect(routes.SetUpTradeDetails.present())
          }
          case _ => Future.successful {
            Logger.error("Could not find expected data in cache on dispose submit - now redirecting...")
            Redirect(routes.VehicleLookup.present())
          }
        }
      }
    )
  }

  private def redirectToVehicleLookup(message: String) = {
    Logger.warn(message)
    Redirect(routes.VehicleLookup.present())
  }

    def back = Action { implicit request =>
    request.cookies.getModel[NewKeeperDetailsViewModel] match {
      case Some(keeperDetails) =>
        if (keeperDetails.address.uprn.isDefined) Redirect(routes.NewKeeperChooseYourAddress.present())
        else Redirect(routes.NewKeeperEnterAddressManually.present())
      case None => Redirect(routes.VehicleLookup.present())
    }
  }

  private def formWithReplacedErrors(form: Form[CompleteAndConfirmFormModel]) = {
    form.replaceError(
      ConsentId,
      "error.required",
      FormError(key = ConsentId, message = "acquire_keeperdetailscomplete.consentError", args = Seq.empty)
    ).replaceError(
        MileageId,
        "error.number",
        FormError(key = MileageId, message = "acquire_privatekeeperdetailscomplete.mileage.validation", args = Seq.empty)
      ).distinctErrors
  }

  private def acquireAction(completeAndConfirmForm: CompleteAndConfirmFormModel,
                            newKeeperDetailsView: NewKeeperDetailsViewModel,
                            vehicleLookup: VehicleLookupFormModel,
                            vehicleDetails: VehicleDetailsModel,
                            traderDetails: TraderDetailsModel,
                            trackingId: String)
                           (implicit request: Request[AnyContent]): Future[Result] = {

    val transactionTimestamp = dateService.now.toDateTime

    val disposeRequest = buildMicroServiceRequest(vehicleLookup, completeAndConfirmForm,
      newKeeperDetailsView, traderDetails, transactionTimestamp)

    webService.invoke(disposeRequest, trackingId).map {
      case (httpResponseCode, response) =>
          Some(Redirect(nextPage(httpResponseCode, response))).
          map(_.withCookie(AcquireCompletionViewModel(vehicleDetails,
                                                      traderDetails,
                                                      newKeeperDetailsView,
                                                      completeAndConfirmForm,
                                                      response.get.transactionId,
                                                      transactionTimestamp))).
          get
    }.recover {
      case e: Throwable =>
        Logger.warn(s"Acquire micro-service call failed.", e)
        Redirect(routes.MicroServiceError.present())
    }
  }

  def nextPage(httpResponseCode: Int, response: Option[AcquireResponseDto]) =
    response match {
      case Some(r) if r.responseCode.isDefined => handleResponseCode(r.responseCode.get)
      case _ => handleHttpStatusCode(httpResponseCode)
    }

  def buildMicroServiceRequest(vehicleLookup: VehicleLookupFormModel,
                               completeAndConfirmFormModel: CompleteAndConfirmFormModel,
                               newKeeperDetailsViewModel: NewKeeperDetailsViewModel,
                               traderDetailsModel: TraderDetailsModel, timestamp: DateTime): AcquireRequestDto = {

    val keeperDetails = buildKeeperDetails(newKeeperDetailsViewModel)

    val traderAddress = traderDetailsModel.traderAddress.address
    val traderDetails = TraderDetailsDto(traderOrganisationName = traderDetailsModel.traderName,
      traderAddressLines = getAddressLines(traderAddress, 4),
      traderPostTown = getPostTownFromAddress(traderAddress).getOrElse(""),
      traderPostCode = getPostCodeFromAddress(traderAddress).getOrElse(""),
      traderEmailAddress = traderDetailsModel.traderEmail)

    val dateTimeFormatter = ISODateTimeFormat.dateTime()

    AcquireRequestDto(referenceNumber = vehicleLookup.referenceNumber,
      registrationNumber = vehicleLookup.registrationNumber,
      keeperDetails,
      traderDetails,
      fleetNumber = newKeeperDetailsViewModel.fleetNumber,
      dateOfTransfer = dateTimeFormatter.print(completeAndConfirmFormModel.dateOfSale.toDateTimeAtStartOfDay),
      mileage = completeAndConfirmFormModel.mileage,
      keeperConsent = consentToBoolean(completeAndConfirmFormModel.consent),
      transactionTimestamp = dateTimeFormatter.print(timestamp),
      requiresSorn = false
    )

  }

  private def buildTitle (titleType: Option[TitleType]): TitleTypeDto = {
    titleType match {
      case Some(title) => title.other match {
        case "" => TitleTypeDto(Some(title.titleType), None)
        case _ => TitleTypeDto(Some(title.titleType), Some(title.other))
      }
      case None => TitleTypeDto(None, None)
    }
  }

  def buildKeeperDetails(newKeeperDetailsViewModel: NewKeeperDetailsViewModel) :KeeperDetailsDto = {
    val keeperAddress = newKeeperDetailsViewModel.address.address

    val dateOfBirth = newKeeperDetailsViewModel.dateOfBirth match {
      case Some(date) => Some(date.toDateTimeAtStartOfDay.toString)
      case _ => None
    }

    KeeperDetailsDto(keeperTitle = buildTitle(newKeeperDetailsViewModel.title),
      KeeperBusinessName = newKeeperDetailsViewModel.businessName,
      keeperForename = newKeeperDetailsViewModel.firstName,
      keeperSurname = newKeeperDetailsViewModel.lastName,
      keeperDateOfBirth = dateOfBirth,
      keeperAddressLines = getAddressLines(keeperAddress, 4),
      keeperPostTown = getPostTownFromAddress(keeperAddress).getOrElse(""),
      keeperPostCode = getPostCodeFromAddress(keeperAddress).getOrElse(""),
      keeperEmailAddress = newKeeperDetailsViewModel.email,
      keeperDriverNumber = newKeeperDetailsViewModel.driverNumber)
  }

  def handleResponseCode(acquireResponseCode: String): Call =
    acquireResponseCode match {
      case "ms.vehiclesService.error.generalError" =>
        Logger.warn("Acquire soap endpoint redirecting to acquire failure page")
        routes.AcquireFailure.present()
      case _ =>
        Logger.warn(s"Acquire micro-service failed so now redirecting to micro service error page. " +
          s"Code returned from ms was $acquireResponseCode")
        routes.MicroServiceError.present()
    }

  def handleHttpStatusCode(statusCode: Int): Call =
    statusCode match {
      case OK =>
        routes.AcquireSuccess.present()
      case _ =>
        routes.MicroServiceError.present()
    }

  def consentToBoolean (consent: String): Boolean = {
    consent == "true"
  }

  def getPostCodeFromAddress (address: Seq[String]): Option[String] = {
    Option(address.last.replace(" ",""))
  }

  def getPostTownFromAddress (address: Seq[String]): Option[String] = {
    Option(address.takeRight(2).head)
  }

  def getAddressLines(address: Seq[String], lines: Int): Seq[String] = {
    val excludeLines = 2
    val getLines = if (lines <= address.length - excludeLines) lines else address.length - excludeLines
    address.take(getLines)
  }
}
