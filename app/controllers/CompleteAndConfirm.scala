package controllers

import com.google.inject.Inject
import email.EmailMessageBuilder
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey
import models.CompleteAndConfirmFormModel.Form.{MileageId, ConsentId}
import models.CompleteAndConfirmResponseModel
import models.CompleteAndConfirmViewModel
import models.VehicleLookupFormModel
import models.VehicleNewKeeperCompletionCacheKeys
import models.VehicleTaxOrSornFormModel
import org.joda.time.{DateTimeZone, DateTime, LocalDate}
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{FormError, Form}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Call, Controller, Request, Result}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.LogFormats.{anonymize, logMessage, optionNone}
import common.mappings.TitleType
import common.model.{NewKeeperDetailsViewModel, TraderDetailsModel, VehicleAndKeeperDetailsModel}
import common.services.{SEND, DateService}
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.acquire.AcquireRequestDto
import common.webserviceclients.acquire.AcquireResponseDto
import common.webserviceclients.acquire.AcquireService
import common.webserviceclients.acquire.KeeperDetailsDto
import common.webserviceclients.acquire.TitleTypeDto
import common.webserviceclients.acquire.TraderDetailsDto
import common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import utils.helpers.Config
import views.html.acquire.complete_and_confirm
import webserviceclients.emailservice.EmailService

class CompleteAndConfirm @Inject()(webService: AcquireService,
                                   emailService: EmailService)
                                  (implicit clientSideSessionFactory: ClientSideSessionFactory,
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
            dateService)
          )
        case _ =>
          redirectToVehicleLookup(NoCookiesFoundMessage).discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey)
      }
    }
  }

  // The dates are valid if they are the same or if the disposal date is before the acquisition date
  def submitWithDateCheck = submitBase(
    (keeperEndDate, dateOfSale) =>
      keeperEndDate.toLocalDate.isEqual(dateOfSale) || keeperEndDate.toLocalDate.isBefore(dateOfSale)
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
              Logger.warn(logMessage("Could not find expected data in cache on dispose submit - now redirecting...",
                request.cookies.trackingId()))
              Redirect(routes.VehicleLookup.present()).discardingCookies()
          }
        },
        validForm => {
          // Check to see if the TraderDetailsModel cookie is present
          request.cookies.getModel[TraderDetailsModel].fold {
            Logger.warn(logMessage("Could not find trader details in cache on Acquire submit - " +
              "now redirecting to SetUpTradeDetails...", request.cookies.trackingId()))
            Future.successful {
              Redirect(routes.SetUpTradeDetails.present()).discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey)
            }
          } { traderDetails => // The TraderDetailsModel cookie is present
            val result = for {
              newKeeperDetails <- request.cookies.getModel[NewKeeperDetailsViewModel]
              vehicleLookup <- request.cookies.getModel[VehicleLookupFormModel]
              vehicleAndKeeperDetails <- request.cookies.getModel[VehicleAndKeeperDetailsModel]
              taxOrSorn <- request.cookies.getModel[VehicleTaxOrSornFormModel]
            } yield {
              // Check to see if the keeperEndDate option is present. This is the date of disposal
              vehicleAndKeeperDetails.keeperEndDate.fold(dateValidCall(validForm, // Here the date is missing so we call the acquire service and move to the next page
                newKeeperDetails,
                vehicleLookup,
                vehicleAndKeeperDetails,
                traderDetails,
                taxOrSorn
              ))(keeperEndDate => { // keeperEndDate is present so we use it to see if the dateOfSale is valid
                if (!validDates(keeperEndDate, validForm.dateOfSale)) {
                  // Date of sale is invalid so send a bad request back to the submitting page
                  Logger.debug(logMessage(s"Complete-and-confirm date validation failed: keeperEndDate " +
                    s"(${keeperEndDate.toLocalDate}) is after dateOfSale (${validForm.dateOfSale})",
                    request.cookies.trackingId()))

                  val dateInvalidCall = Future.successful {
                    BadRequest(complete_and_confirm(
                      CompleteAndConfirmViewModel(form.fill(validForm),
                        vehicleAndKeeperDetails,
                        newKeeperDetails,
                        taxOrSorn,
                        isSaleDateBeforeDisposalDate = true, // This will tell the page to display the date warning
                        submitAction = controllers.routes.CompleteAndConfirm.submitNoDateCheck(), // Next time the submit will not perform any date check
                        dateOfDisposal = Some(keeperEndDate.toString("dd/MM/yyyy"))), // Pass the dateOfDisposal so we can tell the user in the warning
                      dateService)
                    )
                  }
                  dateInvalidCall // Return the bad request
                }
                else dateValidCall(validForm, // Date of sale is valid so call the acquire service and move to the next page
                  newKeeperDetails,
                  vehicleLookup,
                  vehicleAndKeeperDetails,
                  traderDetails,
                  taxOrSorn
                )
              })
            }
            // For comprehension drops out to here if any of the cookies are missing
            result.getOrElse(Future.successful {
              Logger.warn(logMessage("Could not find expected data in cache on acquire submit - " +
                s"now redirecting to VehicleLookup", request.cookies.trackingId()))
              Redirect(routes.VehicleLookup.present()).discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey)
            })
          }
        }
      )
    }
  }

  private def canPerformPresent(action: => Result)(implicit request: Request[_]) =
    request.cookies.getString(AllowGoingToCompleteAndConfirmPageCacheKey).fold {
      Logger.warn(logMessage(s"Could not find AllowGoingToCompleteAndConfirmPageCacheKey in the request. " +
        s"Redirect to VehicleLookup discarding cookies $cookiesToBeDiscardedOnRedirectAway",
        request.cookies.trackingId()))
      Redirect(routes.VehicleLookup.present()).discardingCookies(cookiesToBeDiscardedOnRedirectAway)
    }(c => action)

  private def canPerformSubmit(action: => Future[Result])(implicit request: Request[_]) =
    request.cookies.getString(AllowGoingToCompleteAndConfirmPageCacheKey).fold {
      Logger.warn(logMessage(s"Could not find AllowGoingToCompleteAndConfirmPageCacheKey in the request. " +
        s"Redirect to VehicleLookup discarding cookies $cookiesToBeDiscardedOnRedirectAway",
        request.cookies.trackingId()))
      Future.successful(Redirect(routes.VehicleLookup.present()).discardingCookies(cookiesToBeDiscardedOnRedirectAway))
    }(c => action)

  private def redirectToVehicleLookup(message: String)(implicit request: Request[_]) = {
    Logger.warn(logMessage(message, request.cookies.trackingId()))
    Redirect(routes.VehicleLookup.present())
  }

  def back = Action { implicit request =>
    request.cookies.getModel[NewKeeperDetailsViewModel] match {
      case Some(keeperDetails) =>
        if (keeperDetails.address.uprn.isDefined) {
          Logger.debug(logMessage(s"Redirecting to ${routes.NewKeeperChooseYourAddress.present()}",
            request.cookies.trackingId()))
          Redirect(routes.NewKeeperChooseYourAddress.present())
        }
        else {
          Logger.debug(logMessage(s"Redirecting to ${routes.NewKeeperEnterAddressManually.present()}",
            request.cookies.trackingId()))
          Redirect(routes.NewKeeperEnterAddressManually.present())
        }
      case None => {
        Logger.debug(logMessage(s"Redirecting to ${routes.VehicleLookup.present()}", request.cookies.trackingId()))
        Redirect(routes.VehicleLookup.present())
      }
    }
  }

  private def formWithReplacedErrors(form: Form[CompleteAndConfirmFormModel]) =
    form.replaceError(
      ConsentId,
      "error.required",
      FormError(key = ConsentId, message = "acquire_keeperdetailscomplete.consentError", args = Seq.empty)
    ).replaceError(
        MileageId,
        "error.number",
        FormError(key = MileageId, message = "acquire_privatekeeperdetailscomplete.mileage.validation", args = Seq.empty)
      ).distinctErrors

  private def dateValidCall(validForm: CompleteAndConfirmFormModel,
                            newKeeperDetails: NewKeeperDetailsViewModel,
                            vehicleLookup: VehicleLookupFormModel,
                            vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
                            traderDetails: TraderDetailsModel,
                            taxOrSorn: VehicleTaxOrSornFormModel
                             )
                           (implicit request: Request[AnyContent]): Future[Result] =
    acquireAction(
      validForm,
      newKeeperDetails,
      vehicleLookup,
      vehicleAndKeeperDetails,
      traderDetails,
      taxOrSorn,
      request.cookies.trackingId()
    ).map(_.discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey))

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

    logRequest(disposeRequest)

    webService.invoke(disposeRequest, trackingId).map {
      case (httpResponseCode, response) =>
        Some(Redirect(nextPage(httpResponseCode, response)(vehicleAndKeeperDetails, newKeeperDetailsView, trackingId)))
          .map(_.withCookie(CompleteAndConfirmResponseModel(response.get.transactionId, transactionTimestamp)))
          .map(_.withCookie(completeAndConfirmForm))
          .get
    }.recover {
      case e: Throwable =>
        Logger.warn(s"Acquire micro-service call failed. - trackingId: ${request.cookies.trackingId()}", e)
        Redirect(routes.MicroServiceError.present())
    }
  }

  def nextPage(httpResponseCode: Int, response: Option[AcquireResponseDto])(vehicleDetails: VehicleAndKeeperDetailsModel,
                                                                            keeperDetails: NewKeeperDetailsViewModel,
                                                                            trackingId: String)
                                                                            (implicit request: Request[_]) = {
    response.foreach(r => logResponse(r))

    response match {
      case Some(r) if r.responseCode.isDefined => successReturn(vehicleDetails, keeperDetails, trackingId)
      case _ => handleHttpStatusCode(httpResponseCode)(vehicleDetails, keeperDetails, trackingId)
    }

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
      dateOfTransfer = dateTimeFormatter.print(
        completeAndConfirmFormModel.dateOfSale.toDateTimeAtStartOfDay(DateTimeZone.forID("UTC"))
      ),
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

  def handleHttpStatusCode(statusCode: Int)(vehicleDetails: VehicleAndKeeperDetailsModel,
                                            keeperDetails: NewKeeperDetailsViewModel,
                                            trackingId: String)
                          (implicit request: Request[_]): Call =
    statusCode match {
      case OK => successReturn(vehicleDetails, keeperDetails, trackingId)
      case _ => {
        Logger.warn(s"Acquire micro-service call failed. ${statusCode} - " +
          s"trackingId: ${request.cookies.trackingId()}")
        routes.MicroServiceError.present()
      }
    }

  private def successReturn(vehicleDetails: VehicleAndKeeperDetailsModel,
                            keeperDetails: NewKeeperDetailsViewModel,
                            trackingId: String)
                           (implicit request: Request[_]): Call = {
    createAndSendEmail(vehicleDetails, keeperDetails, trackingId)
    Logger.debug(logMessage(s"Redirecting to ${routes.AcquireSuccess.present()}",request.cookies.trackingId()))
    routes.AcquireSuccess.present()
  }

  def checkboxValueToBoolean (checkboxValue: String): Boolean =
    checkboxValue == "true"

  def getPostCodeFromAddress (address: Seq[String]): Option[String] =
    Option(address.last.replace(" ",""))

  def getPostTownFromAddress (address: Seq[String]): Option[String] =
    Option(address.takeRight(2).head)

  def getAddressLines(address: Seq[String], lines: Int): Seq[String] = {
    val excludeLines = 2
    val getLines = if (lines <= address.length - excludeLines) lines else address.length - excludeLines
    address.take(getLines)
  }

  private def buildWebHeader(trackingId: String): VssWebHeaderDto =
    VssWebHeaderDto(transactionId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser())

  private def buildEndUser(): VssWebEndUserDto =
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)

  private def logRequest(disposeRequest: AcquireRequestDto)(implicit request: Request[_]) = {
    Logger.debug(logMessage("Change keeper micro-service request",
      request.cookies.trackingId(),
      Seq(
        disposeRequest.webHeader.applicationCode,
        disposeRequest.webHeader.originDateTime.toString(),
        disposeRequest.webHeader.serviceTypeCode,
        disposeRequest.webHeader.transactionId,
        disposeRequest.dateOfTransfer,
        disposeRequest.fleetNumber.getOrElse(optionNone),
        disposeRequest.keeperConsent.toString,
        disposeRequest.mileage.toString,
        anonymize(disposeRequest.referenceNumber),
        anonymize(disposeRequest.registrationNumber),
        disposeRequest.requiresSorn.toString,
        anonymize(disposeRequest.traderDetails.get.traderOrganisationName)) ++
        disposeRequest.traderDetails.get.traderAddressLines.map(addr => anonymize(addr)) ++
        Seq(
          anonymize(disposeRequest.traderDetails.get.traderEmailAddress),
          anonymize(disposeRequest.traderDetails.get.traderPostCode),
          anonymize(disposeRequest.traderDetails.get.traderPostTown),
          disposeRequest.transactionTimestamp
        )
      )
    )
  }

  private def logResponse(disposeResponse: AcquireResponseDto)(implicit request: Request[_]) = {
    Logger.debug(logMessage("Change keeper micro-service request",
      request.cookies.trackingId(),
      Seq(anonymize(disposeResponse.registrationNumber),
      disposeResponse.responseCode.getOrElse(""),
      anonymize(disposeResponse.transactionId)))
    )
  }

  /**
   * Calling this method on a successful submission, will send an email if we have the new keeper details.
   * @param keeperDetails the keeper model from the cookie.
   * @return
   */
  def createAndSendEmail(vehicleDetails: VehicleAndKeeperDetailsModel,
                         keeperDetails: NewKeeperDetailsViewModel,
                         trackingId: String)(implicit request: Request[_]) =
    keeperDetails.email match {
      case Some(emailAddr) =>
        import scala.language.postfixOps

        import SEND._ // Keep this local so that we don't pollute rest of the class with unnecessary imports.

        implicit val emailConfiguration = config.emailConfiguration
        implicit val implicitEmailService = implicitly[EmailService](emailService)

        val template = EmailMessageBuilder.buildWith()

        Logger.info(s"Email sent - trackingId: ${request.cookies.trackingId()}")

        // This sends the email.
        SEND email template withSubject vehicleDetails.registrationNumber to emailAddr send trackingId

      case None => Logger.warn(s"tried to send an email with no keeper details - " +
        s"trackingId: ${request.cookies.trackingId()}")
    }
}