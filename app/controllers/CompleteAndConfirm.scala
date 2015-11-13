package controllers

import com.google.inject.Inject
import email.EmailMessageBuilder
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.CompleteAndConfirmFormModel.AllowGoingToCompleteAndConfirmPageCacheKey
import models.CompleteAndConfirmFormModel.Form.{ConsentId, MileageId}
import models.{CompleteAndConfirmFormModel, CompleteAndConfirmResponseModel, CompleteAndConfirmViewModel, VehicleLookupFormModel}
import models.{VehicleNewKeeperCompletionCacheKeys, VehicleTaxOrSornFormModel}
import models.VehicleTaxOrSornFormModel.Form.{NeitherId, SornId, TaxId}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent, Call, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.LogFormats.{DVLALogger, anonymize, optionNone}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClientSideSessionFactory, TrackingId}
import uk.gov.dvla.vehicles.presentation.common.mappings.TitleType
import uk.gov.dvla.vehicles.presentation.common.model.{NewKeeperDetailsViewModel, TraderDetailsModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.services.{DateService, SEND}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.{AcquireRequestDto, AcquireResponseDto, AcquireService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.{KeeperDetailsDto, TitleTypeDto, TraderDetailsDto}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.EmailService
import utils.helpers.Config
import views.html.acquire.complete_and_confirm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompleteAndConfirm @Inject()(webService: AcquireService,
                                   emailService: EmailService)
                                  (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                               dateService: DateService,
                                                               config: Config) extends Controller with DVLALogger {
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
          logMessage(request.cookies.trackingId(), Info, "Presenting complete and confirm view")
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
              logMessage(request.cookies.trackingId(), Warn,
                "Could not find expected data in cache on dispose submit - now redirecting...")
              Redirect(routes.VehicleLookup.present()).discardingCookies()
          }
        },
        validForm => {
          // Check to see if the TraderDetailsModel cookie is present
          request.cookies.getModel[TraderDetailsModel].fold {
            logMessage(request.cookies.trackingId(), Warn,
              "Could not find trader details in cache on Acquire submit - now redirecting to SetUpTradeDetails...")
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
                  logMessage(request.cookies.trackingId(), Debug,
                    "Complete-and-confirm date validation failed: keeperEndDate " +
                    s"(${keeperEndDate.toLocalDate}) is after dateOfSale (${validForm.dateOfSale})")

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
              logMessage(request.cookies.trackingId(), Warn,
                "Could not find expected data in cache on acquire submit - now redirecting to VehicleLookup")
              Redirect(routes.VehicleLookup.present()).discardingCookie(AllowGoingToCompleteAndConfirmPageCacheKey)
            })
          }
        }
      )
    }
  }

  private def canPerformPresent(action: => Result)(implicit request: Request[_]) =
    request.cookies.getString(AllowGoingToCompleteAndConfirmPageCacheKey).fold {
      logMessage(request.cookies.trackingId(), Warn,
        "Could not find AllowGoingToCompleteAndConfirmPageCacheKey in the request. " +
        s"Redirecting to VehicleLookup discarding cookies $cookiesToBeDiscardedOnRedirectAway")
      Redirect(routes.VehicleLookup.present()).discardingCookies(cookiesToBeDiscardedOnRedirectAway)
    }(c => action)

  private def canPerformSubmit(action: => Future[Result])(implicit request: Request[_]) =
    request.cookies.getString(AllowGoingToCompleteAndConfirmPageCacheKey).fold {
      logMessage(request.cookies.trackingId(), Warn,
        "Could not find AllowGoingToCompleteAndConfirmPageCacheKey in the request. " +
        s"Redirecting to VehicleLookup discarding cookies $cookiesToBeDiscardedOnRedirectAway")
      Future.successful(Redirect(routes.VehicleLookup.present()).discardingCookies(cookiesToBeDiscardedOnRedirectAway))
    }(c => action)

  private def redirectToVehicleLookup(message: String)(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(), Warn, message)
    Redirect(routes.VehicleLookup.present())
  }

  def back = Action { implicit request =>
    request.cookies.getModel[NewKeeperDetailsViewModel] match {
      case Some(keeperDetails) =>
        if (keeperDetails.address.uprn.isDefined) {
          logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.NewKeeperChooseYourAddress.present()}")
          Redirect(routes.NewKeeperChooseYourAddress.present())
        }
        else {
          logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.NewKeeperEnterAddressManually.present()}")
          Redirect(routes.NewKeeperEnterAddressManually.present())
        }
      case None =>
        logMessage(request.cookies.trackingId(), Debug, s"Redirecting to ${routes.VehicleLookup.present()}")
        Redirect(routes.VehicleLookup.present())
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
                            taxOrSorn: VehicleTaxOrSornFormModel)
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
                            trackingId: TrackingId)
                           (implicit request: Request[AnyContent]): Future[Result] = {

    val transactionTimestamp = dateService.now.toDateTime

    val acquireRequest = buildMicroServiceRequest(vehicleLookup, completeAndConfirmForm,
      newKeeperDetailsView, traderDetails, taxOrSorn, transactionTimestamp, trackingId)

    logRequest(acquireRequest)

    webService.invoke(acquireRequest, trackingId).map {
      case (httpResponseCode, response) =>
        Some(Redirect(nextPage(httpResponseCode, response)
          (acquireRequest, vehicleAndKeeperDetails, newKeeperDetailsView, transactionTimestamp, trackingId)))
          .map(_.withCookie(CompleteAndConfirmResponseModel(response.get.acquireResponse.transactionId, transactionTimestamp)))
          .map(_.withCookie(completeAndConfirmForm))
          .get
    }.recover {
      case e: Throwable =>
        logMessage(request.cookies.trackingId(), Warn, s"Acquire micro-service call failed: ${e.getMessage}")
        Redirect(routes.MicroServiceError.present())
    }
  }

  def nextPage(httpResponseCode: Int, response: Option[AcquireResponseDto])
              (acquireRequest: AcquireRequestDto,
               vehicleDetails: VehicleAndKeeperDetailsModel,
               keeperDetails: NewKeeperDetailsViewModel,
               transactionTimestamp: DateTime,
               trackingId: TrackingId)(implicit request: Request[_]) = {
    response.foreach(r => logResponse(r))

    // NOTE: This is correct behaviour. Any response, regardless of http status, should be treated as a success.
    (httpResponseCode, response) match {
      case (_, Some(r)) =>
        for { ms <- r.response } yield {
          if (ms.message == "ms.vehiclesService.response.furtherActionRequired") {
            logRequestRequiringFurtherAction(ms.code, r.acquireResponse.transactionId, acquireRequest)
            createAndSendEmailRequiringFurtherAction(r.acquireResponse.transactionId, acquireRequest)
          }
        }
        successReturn(vehicleDetails, keeperDetails, r.acquireResponse.transactionId, transactionTimestamp, trackingId)
      case _ => // Should only be Service Unavailable
        logMessage(request.cookies.trackingId(), Warn, s"Acquire micro-service call failed. $httpResponseCode")
        routes.MicroServiceError.present()
    }
  }

  def buildMicroServiceRequest(vehicleLookup: VehicleLookupFormModel,
                               completeAndConfirmFormModel: CompleteAndConfirmFormModel,
                               newKeeperDetailsViewModel: NewKeeperDetailsViewModel,
                               traderDetailsModel: TraderDetailsModel, taxOrSornModel: VehicleTaxOrSornFormModel,
                               timestamp: DateTime,
                               trackingId: TrackingId): AcquireRequestDto = {

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
      requiresSorn = taxOrSornModel.select == SornId
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
      case Some(date) => Some(ISODateTimeFormat.dateTime().print(date.toDateTimeAtStartOfDay(DateTimeZone.forID("UTC"))))
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

  private def successReturn(vehicleDetails: VehicleAndKeeperDetailsModel,
                            keeperDetails: NewKeeperDetailsViewModel,
                            transactionId: String,
                            transactionTimestamp: DateTime,
                            trackingId: TrackingId)
                           (implicit request: Request[_]): Call = {
    createAndSendEmail(vehicleDetails, keeperDetails, transactionId, transactionTimestamp, trackingId)
    logMessage(request.cookies.trackingId(), Info, s"Redirecting to ${routes.AcquireSuccess.present()}")
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

  private def buildWebHeader(trackingId: TrackingId): VssWebHeaderDto =
    VssWebHeaderDto(transactionId = trackingId.value,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser())

  private def buildEndUser(): VssWebEndUserDto =
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)

  private def logRequest(acquireRequest: AcquireRequestDto)(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(), Debug, "Change keeper micro-service request",
      Option(Seq(
        acquireRequest.webHeader.applicationCode,
        acquireRequest.webHeader.originDateTime.toString,
        acquireRequest.webHeader.serviceTypeCode,
        acquireRequest.webHeader.transactionId,
        acquireRequest.dateOfTransfer,
        acquireRequest.fleetNumber.getOrElse(optionNone),
        acquireRequest.keeperConsent.toString,
        acquireRequest.mileage.toString,
        anonymize(acquireRequest.referenceNumber),
        anonymize(acquireRequest.registrationNumber),
        acquireRequest.requiresSorn.toString,
        anonymize(acquireRequest.traderDetails.get.traderOrganisationName)) ++
        acquireRequest.traderDetails.get.traderAddressLines.map(addr => anonymize(addr)) ++
        Seq(
          anonymize(acquireRequest.traderDetails.get.traderEmailAddress),
          anonymize(acquireRequest.traderDetails.get.traderPostCode),
          anonymize(acquireRequest.traderDetails.get.traderPostTown),
          acquireRequest.transactionTimestamp
        )
      )
    )
  }

  private def logResponse(response: AcquireResponseDto)(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(), Debug, "Change keeper micro-service request",
      Option(Seq(anonymize(response.acquireResponse.registrationNumber),
      s"${response.response.map(_.message)}",
      anonymize(response.acquireResponse.transactionId)))
    )
  }

  private def logRequestRequiringFurtherAction(responseCode: String, transactionId: String,
                                               acquireRequest: AcquireRequestDto)(implicit request: Request[_]) = {
    logMessage(request.cookies.trackingId(), Error, "Further action required",
      Some(Seq(
        acquireRequest.webHeader.applicationCode,
        acquireRequest.webHeader.originDateTime.toString,
        acquireRequest.webHeader.serviceTypeCode,
        transactionId,
        acquireRequest.dateOfTransfer,
        acquireRequest.fleetNumber.getOrElse(optionNone),
        acquireRequest.keeperConsent.toString,
        acquireRequest.mileage.toString,
        anonymize(acquireRequest.referenceNumber),
        anonymize(acquireRequest.registrationNumber),
        acquireRequest.requiresSorn.toString,
        anonymize(acquireRequest.traderDetails.get.traderOrganisationName)) ++
        acquireRequest.traderDetails.get.traderAddressLines.map(addr => anonymize(addr)) ++
        Seq(
          anonymize(acquireRequest.traderDetails.get.traderEmailAddress),
          anonymize(acquireRequest.traderDetails.get.traderPostCode),
          anonymize(acquireRequest.traderDetails.get.traderPostTown),
          acquireRequest.transactionTimestamp
        )
      )
    )
  }

  private def createAndSendEmailRequiringFurtherAction(transactionId: String, acquireRequest: AcquireRequestDto)
                                                      (implicit request: Request[_]) = {

    import SEND._ // Keep this local so that we don't pollute rest of the class with unnecessary imports.

    implicit val emailConfiguration = config.emailConfiguration
    implicit val implicitEmailService = implicitly[EmailService](emailService)

    val email = config.emailConfiguration.feedbackEmail.email

    val dateTime = acquireRequest.webHeader.originDateTime.toString("dd/MM/yy HH:mm")

    val htmlTemplateStart = (title: String ) =>
      s"""
         |<!DOCTYPE html>
         |<head>
         |<title>${title}</title>
         |</head>
         |<body>
         |<ul style="padding: 0; list-style-type: none;">
       """.stripMargin

    val htmlTemplateEnd =
      s"""
         |</ul>
         |</body>
         |</html>
      """.stripMargin

    val message1Title = s"Acquire Failure (1 of 2) ${transactionId}"

    val message1Template = (start: (String) => String, end: String, startLine: String, endLine: String) =>
      start(message1Title) +
      s"""
          |${startLine}Vehicle Registration:  ${acquireRequest.registrationNumber}${endLine}
          |${startLine}Transaction ID:  ${transactionId}${endLine}
          |${startLine}Date/Time of Transaction: ${dateTime}${endLine}
      """.stripMargin +
      end

    val message1 = message1Template((_) => "", "", "", "")
    val message1Html = message1Template(htmlTemplateStart, htmlTemplateEnd, "<li>", "</li>")

    val message2Title = s"Acquire Failure (2 of 2) ${transactionId}"

    val message2Template = (start: (String) => String, end: String, startLine: String, endLine: String,
                             address1Sep: String, address1Pad: String, address2Sep: String, address2Pad: String) =>
      start(message2Title) +
      s"""
        |${startLine}New Keeper Title:  ${acquireRequest.keeperDetails.keeperTitle match {
                                             case TitleTypeDto(Some(1), None) => play.api.i18n.Messages("titlePicker.mr")
                                             case TitleTypeDto(Some(2), None) => play.api.i18n.Messages("titlePicker.mrs")
                                             case TitleTypeDto(Some(3), None) => play.api.i18n.Messages("titlePicker.miss")
                                             case TitleTypeDto(Some(4), Some(s)) => s
                                             case TitleTypeDto(None, None) => "NOT ENTERED"
                                           }
                                         }${endLine}
        |${startLine}New Keeper First Name:  ${acquireRequest.keeperDetails.keeperForename.getOrElse("NOT ENTERED")}${endLine}
        |${startLine}New Keeper/Business Last Name:  ${acquireRequest.keeperDetails.keeperSurname.getOrElse("NOT ENTERED")}/${acquireRequest.keeperDetails.keeperBusinessName.getOrElse("NOT ENTERED")}${endLine}
        |${startLine}New Keeper Address:  ${acquireRequest.keeperDetails.keeperAddressLines.mkString(address1Sep + address1Pad)}${endLine}
        |${address1Pad}${acquireRequest.keeperDetails.keeperPostCode}${endLine}
        |${address1Pad}${acquireRequest.keeperDetails.keeperPostTown}${endLine}
        |${startLine}New Keeper Email:  ${acquireRequest.keeperDetails.keeperEmailAddress.getOrElse("NOT ENTERED")}${endLine}
        |${startLine}Date of Birth:  ${acquireRequest.keeperDetails.keeperDateOfBirth match {
                                          case Some(s) => DateTime.parse(s).toString("dd/MM/yy")
                                          case _ => "NOT ENTERED"
                                        }
                                      }${endLine}
        |${startLine}Driving Licence Number:  ${acquireRequest.keeperDetails.keeperDriverNumber.getOrElse("NOT ENTERED")}${endLine}
        |${startLine}Fleet Number:  ${acquireRequest.fleetNumber.getOrElse("NOT ENTERED")}${endLine}
        |${startLine}Trader Name:  ${acquireRequest.traderDetails.get.traderOrganisationName}${endLine}
        |${startLine}Trader Address:  ${acquireRequest.traderDetails.get.traderAddressLines.mkString(address2Sep + address2Pad)}${endLine}
        |${address2Pad}${acquireRequest.traderDetails.get.traderPostCode}${endLine}
        |${address2Pad}${acquireRequest.traderDetails.get.traderPostTown}${endLine}
        |${startLine}Trader Email:  ${acquireRequest.traderDetails.get.traderEmailAddress.getOrElse("NOT ENTERED")}${endLine}
        |${startLine}Document Reference Number: ${acquireRequest.referenceNumber}${endLine}
        |${startLine}Mileage: ${acquireRequest.mileage.getOrElse("NOT ENTERED")}${endLine}
        |${startLine}Date of Sale:  ${DateTime.parse(acquireRequest.dateOfTransfer).toString("dd/MM/yy")}${endLine}
        |${startLine}Tax Choice:  ${request.cookies.getModel[VehicleTaxOrSornFormModel].get.select match {
                                       case TaxId => play.api.i18n.Messages("acquire_vehicleTaxOrSorn.taxVehicle")
                                       case SornId => play.api.i18n.Messages("acquire_vehicleTaxOrSorn.sornNow")
                                       case NeitherId => play.api.i18n.Messages("acquire_vehicleTaxOrSorn.neither")
                                     }
                                   }${endLine}
        |${startLine}Transaction ID:  ${transactionId}${endLine}
        |${startLine}Date/Time of Transaction:  ${dateTime}${endLine}
      """.stripMargin +
      end

    val message2 = message2Template((_) => "", "", "", "", "\n", "                     ",  "\n", "                 ")

    val message2Html = message2Template(htmlTemplateStart, htmlTemplateEnd, "<li>", "</li>",
                                        "</li>", "<li style='padding-left: 11.2em'>",
                                        "</li>", "<li style='padding-left: 8.5em'>")

    SEND
      .email(Contents(message1Html, message1))
      .withSubject(message1Title)
      .to(email)
      .send(request.cookies.trackingId)

    SEND
      .email(Contents(message2Html, message2))
      .withSubject(message2Title)
      .to(email)
      .send(request.cookies.trackingId)
  }

  /**
   * Calling this method on a successful submission, will send an email if we have the new keeper details.
   * @param keeperDetails the keeper model from the cookie.
   * @return
   */
  def createAndSendEmail(vehicleDetails: VehicleAndKeeperDetailsModel,
                         keeperDetails: NewKeeperDetailsViewModel,
                         transactionId: String,
                         transactionTimestamp: DateTime,
                         trackingId: TrackingId)(implicit request: Request[_]) =
    keeperDetails.email match {
      case Some(emailAddr) =>
        import SEND._

        import scala.language.postfixOps // Keep this local so that we don't pollute rest of the class with unnecessary imports.

        implicit val emailConfiguration = config.emailConfiguration
        implicit val implicitEmailService = implicitly[EmailService](emailService)

        val template = EmailMessageBuilder.buildWith(vehicleDetails, transactionId,
          config.imagesPath, transactionTimestamp)

        // This sends the email.
        SEND email template withSubject s"${vehicleDetails.registrationNumber} Confirmation of new vehicle keeper" to emailAddr send trackingId

      case None => logMessage(request.cookies.trackingId(), Warn, "Tried to send an email with no keeper details")
    }
}
