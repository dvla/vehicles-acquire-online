package controllers

import akka.actor.Status.Success
import com.google.inject.Inject
import models._
import models.PrivateKeeperDetailsFormModel.Form.ConsentId
import models.CompleteAndConfirmFormModel.Form.MileageId
import models.CompleteAndConfirmFormModel.CompleteAndConfirmCacheTransactionIdCacheKey
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.data.{FormError, Form}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import uk.gov.dvla.vehicles.presentation.common.model.{VehicleDetailsModel, TraderDetailsModel}
import utils.helpers.Config
import views.html.acquire.complete_and_confirm
import webserviceclients.acquire._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompleteAndConfirm @Inject()(webService: AcquireService)(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                               dateService: DateService,
                                               config: Config) extends Controller {

  val formatter = ISODateTimeFormat.dateTime()

  private[controllers] val form = Form(
    CompleteAndConfirmFormModel.Form.Mapping
  )

  private final val NoNewKeeperCookieMessage = "Did not find a new keeper details cookie in cache. " +
    "Now redirecting to Vehicle Lookup."

  def present = Action { implicit request =>
    request.cookies.getModel[NewKeeperDetailsViewModel] match {
      case Some(newKeeperDetails) =>
        Ok(complete_and_confirm(CompleteAndConfirmViewModel(form.fill(), null, newKeeperDetails), dateService))
      case _ => redirectToVehicleLookup(NoNewKeeperCookieMessage)
    }
  }

  // TODO: Add checking for provided values
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
            // TODO : Make sure we redirect to the right place
            Logger.debug("Could not find expected data in cache on dispose submit - now redirecting...")
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm => {
        // TODO : Check that we're doing the correct redirect on error
        // TODO : should probably be a bad request or failure on error
        request.cookies.getModel[NewKeeperDetailsViewModel] match {
          case Some(_) =>
        acquireAction(webService,
        validForm,
        request.cookies.getModel[BusinessKeeperDetailsFormModel],
        request.cookies.getModel[PrivateKeeperDetailsFormModel],
        request.cookies.getModel[NewKeeperDetailsViewModel].get,
        request.cookies.trackingId())
          case _ => Future.successful {Redirect(routes.VehicleLookup.present())}
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

  private def acquireAction(webService: AcquireService,
                            completeAndConfirmFormModel: CompleteAndConfirmFormModel,
                            businessKeeperDetailsFormModel: Option[BusinessKeeperDetailsFormModel],
                            privateKeeperDetailsFormModel: Option[PrivateKeeperDetailsFormModel],
                            newKeeperDetailsViewModel: NewKeeperDetailsViewModel,
                            trackingId: String)
                           (implicit request: Request[AnyContent]): Future[Result] = {

    (request.cookies.getModel[TraderDetailsModel], request.cookies.getModel[VehicleLookupFormModel]) match {
      case (Some(traderDetails), Some(vehicleLookup)) =>
        callMicroService(vehicleLookup,
          completeAndConfirmFormModel,
          businessKeeperDetailsFormModel,
          privateKeeperDetailsFormModel,
          newKeeperDetailsViewModel,
          traderDetails,
          trackingId)
      case _ =>
        Logger.error("Could not find either dealer details or VehicleLookupFormModel in cache on Acquire submit")
        Future(Redirect(routes.SetUpTradeDetails.present()))
    }
  }

    def nextPage(httpResponseCode: Int, response: Option[AcquireResponseDto]) =
      response match {
        case Some(r) if r.responseCode.isDefined => handleResponseCode(r.responseCode.get)
        case _ => handleHttpStatusCode(httpResponseCode)
      }

    def callMicroService(vehicleLookup: VehicleLookupFormModel,
                         completeAndConfirmForm: CompleteAndConfirmFormModel,
                         businessKeeperDetailsFormModel: Option[BusinessKeeperDetailsFormModel],
                         privateKeeperDetailsFormModel: Option[PrivateKeeperDetailsFormModel],
                         newKeeperDetailsViewModel: NewKeeperDetailsViewModel,
                         traderDetails: TraderDetailsModel,
                         trackingId: String): Future[Result] = {

      val disposeRequest = buildMicroServiceRequest(vehicleLookup, completeAndConfirmForm, businessKeeperDetailsFormModel,
        privateKeeperDetailsFormModel, newKeeperDetailsViewModel, traderDetails)

      webService.invoke(disposeRequest, trackingId).map {
        case (httpResponseCode, response) => {
          Some(Redirect(nextPage(httpResponseCode, response))).
//            map(_.withCookie(completeAndConfirmForm)).
//            map(storeResponseInCache(response, _)).
            get
        }
      }.recover {
        case e: Throwable =>
          Logger.warn(s"Dispose micro-service call failed.", e)
          Redirect(routes.MicroServiceError.present())
      }
    }

//    def storeResponseInCache(response: Option[AcquireResponseDto], nextPage: Result): Result =
//      response match {
//        case Some(o) =>
//          val nextPageWithTransactionId =
//            if (!o.transactionId.isEmpty) nextPage.withCookie(CompleteAndConfirmCacheTransactionIdCacheKey, o.transactionId)
//            else nextPage
//
//          if (!o.registrationNumber.isEmpty)
//            nextPageWithTransactionId.withCookie(CompleteAndConfirmCacheTransactionIdCacheKey, o.registrationNumber)
//          else nextPageWithTransactionId
//        case None => nextPage
//      }

    def buildMicroServiceRequest(vehicleLookup: VehicleLookupFormModel,
                                 completeAndConfirmFormModel: CompleteAndConfirmFormModel,
                                 businessKeeperDetailsFormModel: Option[BusinessKeeperDetailsFormModel],
                                 privateKeeperDetailsFormModel: Option[PrivateKeeperDetailsFormModel],
                                 newKeeperDetailsViewModel: NewKeeperDetailsViewModel,
                                 traderDetailsModel: TraderDetailsModel): AcquireRequestDto = {

      // TODO: This ...
      // Get the title from one of the
      val titleType = TitleType(titleType = Some(1), other = Some(""))

      val keeperDetails = buildKeeperDetails(businessKeeperDetailsFormModel,
        privateKeeperDetailsFormModel,
        newKeeperDetailsViewModel,
        titleType)

      val traderAddress = traderDetailsModel.traderAddress.address
      val traderDetails = TraderDetails(traderOrganisationName = traderDetailsModel.traderName,
        traderAddressLines = getAddressLines(traderAddress, 4),
        traderPostTown = getPostTownFromAddress(traderAddress).getOrElse(""),
        traderPostCode = getPostCodeFromAddress(traderAddress).getOrElse(""),
        traderEmailAddress = traderDetailsModel.traderEmail)

      AcquireRequestDto(referenceNumber = vehicleLookup.referenceNumber,
        registrationNumber = vehicleLookup.registrationNumber,
        keeperDetails,
        traderDetails,
        fleetNumber = (businessKeeperDetailsFormModel map (kd => kd.fleetNumber)).flatten,
        //        dateOfTransfer = formatter.print(completeAndConfirmFormModel.dateOfSale),
        dateOfTransfer = "2014-03-04T00:00:00.000Z",
        mileage = completeAndConfirmFormModel.mileage,
        keeperConsent = consentToBoolean(completeAndConfirmFormModel.consent),
        //                        transactionTimestamp = formatter.print(dateService.now.toDateTime)
        transactionTimestamp = "2014-03-04T00:00:00.000Z"
      )

    }

    // TODO : throw an error or something if we don't match the test below
    def buildKeeperDetails(businessKeeperDetailsFormModel: Option[BusinessKeeperDetailsFormModel],
                           privateKeeperDetailsFormModel: Option[PrivateKeeperDetailsFormModel],
                           newKeeperDetailsViewModel: NewKeeperDetailsViewModel,
                           titleType: TitleType) :KeeperDetails = {
      (businessKeeperDetailsFormModel, privateKeeperDetailsFormModel) match {
        case (Some(keeperDetailsModel), None) => buildBusinessKeeperDetails(keeperDetailsModel, newKeeperDetailsViewModel, titleType)
        case (None, Some(keeperDetailsModel)) => buildPrivateKeeperDetails(keeperDetailsModel, newKeeperDetailsViewModel, titleType)
      }
    }

    def buildBusinessKeeperDetails(businessKeeperDetailsFormModel: BusinessKeeperDetailsFormModel,
                                   newKeeperDetailsViewModel: NewKeeperDetailsViewModel,
                                   titleType: TitleType) :KeeperDetails = {

      val keeperAddress = newKeeperDetailsViewModel.address.address

      KeeperDetails(keeperTitle = titleType,
        KeeperBusinessName = Option(businessKeeperDetailsFormModel.businessName),
        keeperForename = None,
        keeperSurname = None,
        keeperDateOfBirth = None,
        keeperAddressLines = getAddressLines(keeperAddress, 4),
        keeperPostTown = getPostTownFromAddress(keeperAddress).getOrElse(""),
        keeperPostCode = getPostCodeFromAddress(keeperAddress).getOrElse(""),
        keeperEmailAddress = businessKeeperDetailsFormModel.email,
        keeperDriverNumber = None)
    }

    def buildPrivateKeeperDetails(privateKeeperDetailsFormModel: PrivateKeeperDetailsFormModel,
                                  newKeeperDetailsViewModel: NewKeeperDetailsViewModel,
                                  titleType: TitleType) :KeeperDetails = {

      val dateOfBirth = privateKeeperDetailsFormModel.dateOfBirth match {
        case Some(dob) => Some(formatter.print(dob))
        case _ => None
      }

      val keeperAddress = newKeeperDetailsViewModel.address.address

      // TODO : Sort out date mappings
      KeeperDetails(keeperTitle = titleType,
        KeeperBusinessName = None,
        keeperForename = Some(privateKeeperDetailsFormModel.firstName),
        keeperSurname = Some(privateKeeperDetailsFormModel.lastName),
        keeperDateOfBirth = Some("2014-03-04T00:00:00.000Z"), //dateOfBirth,
        keeperAddressLines = getAddressLines(keeperAddress, 4),
        keeperPostTown = getPostTownFromAddress(keeperAddress).getOrElse(""),
        keeperPostCode = getPostCodeFromAddress(keeperAddress).getOrElse(""),
        keeperEmailAddress = privateKeeperDetailsFormModel.email,
        keeperDriverNumber = privateKeeperDetailsFormModel.driverNumber)
    }

    def handleResponseCode(acquireResponseCode: String): Call =
    acquireResponseCode match {
      case "ms.vehiclesService.response.unableToProcessApplication" =>
        Logger.warn("Acquire soap endpoint redirecting to acquire failure page")
        // TODO : Redirect this to the correct page
        routes.NotImplemented.present()
      // routes.DisposeFailure.present()
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
