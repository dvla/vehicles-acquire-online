package webserviceclients.acquire_service

import org.joda.time.DateTime
import play.api.libs.json.Json
import helpers.{WithApplication, UnitSpec}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, NoCookieFlags}
import helpers.WireMockFixture
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import webserviceclients.acquire.{TraderDetailsDto, KeeperDetailsDto, TitleTypeDto, AcquireRequestDto, AcquireConfig, AcquireWebServiceImpl}

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}

class AcquireWebServiceImplSpec extends UnitSpec with WireMockFixture {

  implicit val noCookieFlags = new NoCookieFlags
  implicit val clientSideSessionFactory = new ClearTextClientSideSessionFactory()
  val acquireService = new AcquireWebServiceImpl(new AcquireConfig() {
    override val baseUrl = s"http://localhost:$wireMockPort"
  })

  private final val trackingId = "track-id-test"

  implicit val titleTypeFormat = Json.format[TitleTypeDto]
  implicit val keeperDetailsFormat = Json.format[KeeperDetailsDto]
  implicit val traderDetailsFormat = Json.format[TraderDetailsDto]
  implicit val acquireRequestFormat = Json.format[AcquireRequestDto]

  val titleType = TitleTypeDto(Some(1), None)
  val keeperDetails = KeeperDetailsDto(keeperTitle = titleType,
    KeeperBusinessName = None,
    keeperForename = Some("forename"),
    keeperSurname = Some("surname"),
    keeperDateOfBirth = None,
    keeperAddressLines = Seq("a", "b"),
    keeperPostTown = "post town",
    keeperPostCode = "QQ99QQ",
    keeperEmailAddress = None,
    keeperDriverNumber = None)

  val traderDetails = TraderDetailsDto(
    traderOrganisationName = "Org name",
    traderAddressLines = Seq("a", "b"),
    traderPostTown = "post town",
    traderPostCode = "QQ99QQ",
    traderEmailAddress = None)

  val request = AcquireRequestDto(
    referenceNumber = "ref num",
    registrationNumber = "vrm",
    keeperDetails: KeeperDetailsDto,
    traderDetails: TraderDetailsDto,
    fleetNumber = None,
    dateOfTransfer = new DateTime().toString,
    mileage = None,
    keeperConsent = true,
    transactionTimestamp = new DateTime().toString,
    requiresSorn = false)

  "callAcquireService" should {
    "send the serialised json request" in new WithApplication {
      val resultFuture = acquireService.callAcquireService(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/vehicles/acquire/v1")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId)).
          withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }
}
