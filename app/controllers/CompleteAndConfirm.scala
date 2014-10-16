package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import models.{CompleteAndConfirmFormModel, CompleteAndConfirmViewModel, NewKeeperDetailsViewModel}
import models.VehicleLookupFormModel
import models.CompleteAndConfirmFormModel.Form.{MileageId, ConsentId}
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{FormError, Form}
import play.api.mvc.{Action, AnyContent, Call, Controller, Request, Result}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.mappings.TitleType
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.model.{VehicleDetailsModel, TraderDetailsModel}
import utils.helpers.Config
import views.html.acquire.complete_and_confirm
import webserviceclients.acquire.{TitleTypeDto, TraderDetailsDto, AcquireRequestDto, AcquireResponseDto, KeeperDetailsDto, AcquireService}


class CompleteAndConfirm @Inject()(webService: AcquireService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                               dateService: DateService,
                                                               config: Config) extends Controller {

  private[controllers] val form = Form(
    CompleteAndConfirmFormModel.Form.Mapping
  )

  private final val NoNewKeeperCookieMessage = "Did not find a new keeper details cookie in cache. " +
    "Now redirecting to Vehicle Lookup."

  private final val NoCookiesFoundMessage = "Failed to find new keeper details and or vehicle details in cache. " +
    "Now redirecting to vehicle lookup"

  def present = Action { implicit request =>
    val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
    val vehicleDetailsOpt = request.cookies.getModel[VehicleDetailsModel]
    (newKeeperDetailsOpt, vehicleDetailsOpt) match {
      case (Some(newKeeperDetails), Some(vehicleDetails)) =>
        Ok(complete_and_confirm(CompleteAndConfirmViewModel(form.fill(), vehicleDetails, newKeeperDetails), dateService))
      case _ => redirectToVehicleLookup(NoCookiesFoundMessage)
    }
  }

  def submit = Action.async { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => Future.successful {
        val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
        val vehicleDetailsOpt = request.cookies.getModel[VehicleDetailsModel]
        (newKeeperDetailsOpt, vehicleDetailsOpt) match {
          case (Some(newKeeperDetails), Some(vehicleDetails)) =>
            BadRequest(complete_and_confirm(
              CompleteAndConfirmViewModel(formWithReplacedErrors(invalidForm), vehicleDetails, newKeeperDetails), dateService)
            )
          case _ =>
            Logger.debug("Could not find expected data in cache on dispose submit - now redirecting...")
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => {
        val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
        val vehicleDetailsOpt = request.cookies.getModel[VehicleLookupFormModel]
        val traderDetails = request.cookies.getModel[TraderDetailsModel]
        (newKeeperDetailsOpt, vehicleDetailsOpt, traderDetails) match {
          case (Some(newKeeperDetails), Some(vehicleDetails), Some(traderDetails)) =>
            if (config.isMicroserviceIntegrationEnabled){
            acquireAction (validForm,
                           newKeeperDetails,
                           vehicleDetails,
                           traderDetails,
                           request.cookies.trackingId())}
            else {
              Future.successful {
                Logger.error("Microservice integration is disabled")
                Redirect(routes.AcquireSuccess.present()).withCookie(validForm)
              }
            }
          case (_, _, None) => Future.successful {
            Logger.error("Could not find either dealer details or VehicleLookupFormModel in cache on Acquire submit")
            Redirect(routes.SetUpTradeDetails.present())
          }
          case _ => Future.successful {
            Logger.debug("Could not find expected data in cache on dispose submit - now redirecting...")
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
                            traderDetails: TraderDetailsModel,
                            trackingId: String)
                           (implicit request: Request[AnyContent]): Future[Result] = {
    val disposeRequest = buildMicroServiceRequest(vehicleLookup, completeAndConfirmForm,
      newKeeperDetailsView, traderDetails)
    webService.invoke(disposeRequest, trackingId).map {
      case (httpResponseCode, response) => {
        Some(Redirect(nextPage(httpResponseCode, response))).
          map(_.withCookie(completeAndConfirmForm)).
          get
      }
    }.recover {
      case e: Throwable =>
        Logger.warn(s"Dispose micro-service call failed.", e)
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
                               traderDetailsModel: TraderDetailsModel): AcquireRequestDto = {

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
      transactionTimestamp = dateTimeFormatter.print(dateService.now.toDateTime),
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
      case "ms.vehiclesService.response.unableToProcessApplication" =>
        Logger.warn("Acquire soap endpoint redirecting to acquire failure page")
        // TODO : Redirect to error page
        routes.NotImplemented.present()
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
    consent match {
      case "true" => true
      case _ => false
    }
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
