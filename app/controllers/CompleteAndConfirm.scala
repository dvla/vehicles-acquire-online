package controllers

import com.google.inject.Inject
import models.CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey
import models.NewKeeperDetailsViewModel
import models.CompleteAndConfirmResponseModel
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmViewModel
import models.VehicleLookupFormModel
import models.VehicleTaxOrSornFormModel
import models.CompleteAndConfirmFormModel.Form.{MileageId, ConsentId}
import org.joda.time.{LocalDate, DateTime}
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{FormError, Form}
import play.api.mvc.{Action, AnyContent, Call, Controller, Request, Result}
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.mappings.TitleType
import common.model.{VehicleAndKeeperDetailsModel, TraderDetailsModel}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.html.acquire.complete_and_confirm
import common.webserviceclients.acquire.AcquireRequestDto
import common.webserviceclients.acquire.AcquireResponseDto
import common.webserviceclients.acquire.AcquireService
import common.webserviceclients.acquire.KeeperDetailsDto
import common.webserviceclients.acquire.TitleTypeDto
import common.webserviceclients.acquire.TraderDetailsDto
import models.VehicleNewKeeperCompletionCacheKeys

class CompleteAndConfirm @Inject()(webService: AcquireService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                               dateService: DateService,
                                                               config: Config) extends Controller {
  private val cookiesToBeDiscardedOnRedirectAway =
    VehicleNewKeeperCompletionCacheKeys ++ Set(AllowGoingToCompleteAndConfirmPageCacheKey)

  private[controllers] val form = Form(
    CompleteAndConfirmFormModel.Form.detailMapping
  )

  private final val NoCookiesFoundMessage = "Failed to find new keeper details and or vehicle details and or " +
    "vehicle sorn details in cache. Now redirecting to vehicle lookup"

  def present = Action { implicit request =>
    canPerformPresent {
      val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
      val vehicleAndKeeperDetailsOpt = request.cookies.getModel[VehicleAndKeeperDetailsModel]
      val vehicleSornOpt = request.cookies.getModel[VehicleTaxOrSornFormModel]
      (newKeeperDetailsOpt, vehicleAndKeeperDetailsOpt, vehicleSornOpt) match {
        case (Some(newKeeperDetails), Some(vehicleAndKeeperDetails), Some(vehicleSorn)) =>
          Ok(complete_and_confirm(CompleteAndConfirmViewModel(form.fill(),
            vehicleAndKeeperDetails,
            newKeeperDetails,
            vehicleSorn,
            isSaleDateBeforeDisposalDate = false),
            dateService))
        case _ =>
          redirectToVehicleLookup(NoCookiesFoundMessage).discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey)
      }
    }
  }

  def submitWithDateCheck = submitBase(
    (keeperEndDate, dateOfSale) => keeperEndDate.toLocalDate.isBefore(dateOfSale)
  )

  def submitNoDateCheck = submitBase((keeperEndDate, dateOfSale) => true)

  private def submitBase(validDates: (DateTime, LocalDate) => Boolean) = Action.async { implicit request =>
    canPerformSubmit {
      form.bindFromRequest.fold(
        invalidForm => Future.successful {
          val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
          val vehicleAndKeeperDetailsOpt = request.cookies.getModel[VehicleAndKeeperDetailsModel]
          val vehicleSornOpt = request.cookies.getModel[VehicleTaxOrSornFormModel]
          (newKeeperDetailsOpt, vehicleAndKeeperDetailsOpt, vehicleSornOpt) match {
            case (Some(newKeeperDetails), Some(vehicleDetails), Some(vehicleSorn)) =>
              BadRequest(complete_and_confirm(
                CompleteAndConfirmViewModel(formWithReplacedErrors(invalidForm),
                  vehicleDetails,
                  newKeeperDetails,
                  vehicleSorn,
                  isSaleDateBeforeDisposalDate = false),
                dateService)
              )
            case _ =>
              Logger.warn("Could not find expected data in cache on dispose submit - now redirecting...")
              Redirect(routes.VehicleLookup.present()).discardingCookies()
          }
        },
        validForm => {
          val newKeeperDetailsOpt = request.cookies.getModel[NewKeeperDetailsViewModel]
          val vehicleLookupOpt = request.cookies.getModel[VehicleLookupFormModel]
          val vehicleAndKeeperDetailsOpt = request.cookies.getModel[VehicleAndKeeperDetailsModel]
          val traderDetailsOpt = request.cookies.getModel[TraderDetailsModel]
          val taxOrSornOpt = request.cookies.getModel[VehicleTaxOrSornFormModel]
          val validFormResult =
            (newKeeperDetailsOpt, vehicleLookupOpt, vehicleAndKeeperDetailsOpt, traderDetailsOpt, taxOrSornOpt) match {
            case (Some(newKeeperDetails), Some(vehicleLookup), Some(vehicleDetails), Some(traderDetails), Some(taxOrSorn)) =>

              val dateValidCall = acquireAction(
                validForm,
                newKeeperDetails,
                vehicleLookup,
                vehicleDetails,
                traderDetails,
                taxOrSorn,
                request.cookies.trackingId()
              ).map(_.discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey))

              println("######################## Config.googleAnalyticsTrackingId:" + config.googleAnalyticsTrackingId)
              val dateInvalidCall = Future.successful {
                BadRequest(complete_and_confirm(
                  CompleteAndConfirmViewModel(form.fill(validForm),
                    vehicleDetails,
                    newKeeperDetails,
                    taxOrSorn,
                    isSaleDateBeforeDisposalDate = true,
                    submitAction = controllers.routes.CompleteAndConfirm.submitNoDateCheck()),
                  dateService)
                )
              }

              vehicleDetails.keeperEndDate.fold(dateValidCall)(keeperEndDate => {
                if (!validDates(keeperEndDate, validForm.dateOfSale)) {
                  Logger.debug(s"Complete-and-confirm date validation failed: keeperEndDate (${keeperEndDate.toLocalDate}) is after dateOfSale ($validForm.dateOfSale)")
                  dateInvalidCall
                }
                else dateValidCall
              })

            case (_, _, _, None, _) => Future.successful {
              Logger.warn("Could not find either dealer details in cache on Acquire submit - " +
                "now redirecting to SetUpTradeDetails...")
              Redirect(routes.SetUpTradeDetails.present()).discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey)
            }
            case _ => Future.successful {
              Logger.warn("Could not find expected data in cache on dispose submit - now redirecting to VehicleLookup...")
              Redirect(routes.VehicleLookup.present()).discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey)
            }
          }
          validFormResult
        }
      )
    }
  }

  private def canPerformPresent(action: => Result)(implicit request: Request[_]) =
    request.cookies.getString(AllowGoingToCompleteAndConfirmPageCacheKey).fold {
      Logger.warn(s"Could not find AllowGoingToCompleteAndConfirmPageCacheKey in the request. " +
        s"Redirect to VehicleLookup discarding cookies $cookiesToBeDiscardedOnRedirectAway")
      Redirect(routes.VehicleLookup.present()).discardingCookies(cookiesToBeDiscardedOnRedirectAway)
    }(c => action)

  private def canPerformSubmit(action: => Future[Result])(implicit request: Request[_]) =
    request.cookies.getString(AllowGoingToCompleteAndConfirmPageCacheKey).fold {
      Logger.warn(s"Could not find AllowGoingToCompleteAndConfirmPageCacheKey in the request. " +
        s"Redirect to VehicleLookup discarding cookies $cookiesToBeDiscardedOnRedirectAway")
      Future.successful(Redirect(routes.VehicleLookup.present()).discardingCookies(cookiesToBeDiscardedOnRedirectAway))
    }(c => action)

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
                            vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
                            traderDetails: TraderDetailsModel,
                            taxOrSorn: VehicleTaxOrSornFormModel,
                            trackingId: String)
                           (implicit request: Request[AnyContent]): Future[Result] = {

    val transactionTimestamp = dateService.now.toDateTime

    val disposeRequest = buildMicroServiceRequest(vehicleLookup, completeAndConfirmForm,
      newKeeperDetailsView, traderDetails, taxOrSorn, transactionTimestamp, trackingId)

    webService.invoke(disposeRequest, trackingId).map {
      case (httpResponseCode, response) =>
          Some(Redirect(nextPage(httpResponseCode, response)))
            .map(_.withCookie(CompleteAndConfirmResponseModel(response.get.transactionId, transactionTimestamp)))
            .map(_.withCookie(completeAndConfirmForm))
            .get
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
                               traderDetailsModel: TraderDetailsModel, taxOrSornModel: VehicleTaxOrSornFormModel,
                               timestamp: DateTime,
                               trackingId: String): AcquireRequestDto = {

    val keeperDetails = buildKeeperDetails(newKeeperDetailsViewModel)

    val traderAddress = traderDetailsModel.traderAddress.address
    val traderDetails = TraderDetailsDto(traderOrganisationName = traderDetailsModel.traderName,
      traderAddressLines = getAddressLines(traderAddress, 4),
      traderPostTown = getPostTownFromAddress(traderAddress).getOrElse(""),
      traderPostCode = getPostCodeFromAddress(traderAddress).getOrElse(""),
      traderEmailAddress = traderDetailsModel.traderEmail)

    val dateTimeFormatter = ISODateTimeFormat.dateTime()

    AcquireRequestDto(buildWebHeader(trackingId),
      referenceNumber = vehicleLookup.referenceNumber,
      registrationNumber = vehicleLookup.registrationNumber,
      keeperDetails,
      Some(traderDetails),
      fleetNumber = newKeeperDetailsViewModel.fleetNumber,
      dateOfTransfer = dateTimeFormatter.print(completeAndConfirmFormModel.dateOfSale.toDateTimeAtStartOfDay),
      mileage = completeAndConfirmFormModel.mileage,
      keeperConsent = checkboxValueToBoolean(completeAndConfirmFormModel.consent),
      transactionTimestamp = dateTimeFormatter.print(timestamp),
      requiresSorn = checkboxValueToBoolean(taxOrSornModel.sornVehicle.getOrElse("false"))
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

    KeeperDetailsDto(
      keeperTitle = buildTitle(newKeeperDetailsViewModel.title),
      keeperBusinessName = newKeeperDetailsViewModel.businessName,
      keeperForename = newKeeperDetailsViewModel.firstName,
      keeperSurname = newKeeperDetailsViewModel.lastName,
      keeperDateOfBirth = dateOfBirth,
      keeperAddressLines = getAddressLines(keeperAddress, 4),
      keeperPostTown = getPostTownFromAddress(keeperAddress).getOrElse(""),
      keeperPostCode = getPostCodeFromAddress(keeperAddress).getOrElse(""),
      keeperEmailAddress = newKeeperDetailsViewModel.email,
      keeperDriverNumber = newKeeperDetailsViewModel.driverNumber
    )
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

  def checkboxValueToBoolean (checkboxValue: String): Boolean = {
    checkboxValue == "true"
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

  private def buildWebHeader(trackingId: String): VssWebHeaderDto =
  {
    VssWebHeaderDto(transactionId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser())
  }

  private def buildEndUser(): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)
  }
}