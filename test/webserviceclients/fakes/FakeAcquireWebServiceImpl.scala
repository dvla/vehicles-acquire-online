package webserviceclients.fakes

import play.api.Logger
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.webserviceclients
import webserviceclients.acquire.AcquireResponse
import webserviceclients.acquire.AcquireRequestDto
import webserviceclients.acquire.AcquireResponseDto
import webserviceclients.acquire.AcquireWebService

class FakeAcquireWebServiceImpl extends AcquireWebService {
  import FakeAcquireWebServiceImpl._

  override def callAcquireService(request: AcquireRequestDto, trackingId: TrackingId): Future[WSResponse] = Future.successful {
    val (status: Int, acquireResponse: AcquireResponseDto) = {
      request.referenceNumber match {
        case SimulateMicroServiceUnavailable => throw new RuntimeException("simulateMicroServiceUnavailable")
        case SimulateSoapEndpointFailure => (INTERNAL_SERVER_ERROR, acquireResponseSoapEndpointFailure)
        case _ => (OK, acquireResponseSuccess)
      }
    }
    val responseAsJson = Json.toJson(acquireResponse)
    Logger.debug(s"FakeVehicleLookupWebService callVehicleLookupService with: $responseAsJson")
    new FakeResponse(status = status, fakeJson = Some(responseAsJson))
  }
}

object FakeAcquireWebServiceImpl {
  final val TransactionIdValid = "1234"
  private final val AuditIdValid = "7575"
  private final val SimulateMicroServiceUnavailable = "8" * 11
  private final val SimulateSoapEndpointFailure = "9" * 11
  private final val RegistrationNumberValid = "AB12AWR"

  val acquireResponseSuccess = AcquireResponseDto(
    None,
    AcquireResponse(transactionId = TransactionIdValid, registrationNumber = RegistrationNumberValid))

  // No transactionId because the soap endpoint is down
  val acquireResponseSoapEndpointFailure = AcquireResponseDto(
    None,
    AcquireResponse(transactionId = "", registrationNumber = "")
  )

  // We should always get back a transaction id even for failure scenarios. Only exception is if the soap endpoint is down
  val acquireResponseGeneralError = AcquireResponseDto(
    Some(MicroserviceResponse("U0020", "ms.vehiclesService.error.generalError")),
    AcquireResponse(transactionId = TransactionIdValid, registrationNumber = "AA11AAC")
  )

  val acquireResponseApplicationBeingProcessed = AcquireResponseDto(
    None,
    AcquireResponse(transactionId = TransactionIdValid, registrationNumber = RegistrationNumberValid)
  )

  val acquireResponseFurtherActionRequired = AcquireResponseDto(
    Some(MicroserviceResponse("X0001", "ms.vehiclesService.response.furtherActionRequired")),
    AcquireResponse(transactionId = TransactionIdValid, registrationNumber = "")
  )

  final val ConsentValid = "true"
  final val MileageValid = "20000"
  final val MileageInvalid = "INVALID"
}
