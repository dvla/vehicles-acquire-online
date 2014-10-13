package webserviceclients.acquire

import org.joda.time.DateTime
import play.api.libs.json.Json

case class TitleType(titleType: Option[Int], other: Option[String])

object TitleType{
  implicit val JsonFormat = Json.writes[TitleType]
}

final case class KeeperDetails(keeperTitle: TitleType,
                               KeeperBusinessName: Option[String],
                               keeperForename: Option[String],
                               keeperSurname: Option[String],
                               keeperDateOfBirth: Option[String] = None,
                               keeperAddressLines: Seq[String],
                               keeperPostTown: String,
                               keeperPostCode: String,
                               keeperEmailAddress: Option[String],
                               keeperDriverNumber: Option[String])

object KeeperDetails{
  implicit val JsonFormat = Json.writes[KeeperDetails]
}

final case class TraderDetails(traderOrganisationName: String,
                               traderAddressLines: Seq[String],
                               traderPostTown: String,
                               traderPostCode: String,
                               traderEmailAddress: Option[String])

object TraderDetails{
  implicit val JsonFormat = Json.writes[TraderDetails]
}
final case class AcquireRequestDto(referenceNumber: String,
                                   registrationNumber: String,
                                   keeperDetails: KeeperDetails,
                                   traderDetails: TraderDetails,
                                   fleetNumber: Option[String] = None,
                                   dateOfTransfer: String,
                                   mileage: Option[Int],
                                   keeperConsent: Boolean,
                                   transactionTimestamp: String)

object AcquireRequestDto {
  implicit val JsonFormat = Json.writes[AcquireRequestDto]
}


