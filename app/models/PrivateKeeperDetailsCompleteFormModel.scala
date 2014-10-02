package models

import mappings.Consent.consent
import org.joda.time.LocalDate
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Date.{dateMapping, notInTheFuture, optionalDateOfBirth}
import uk.gov.dvla.vehicles.presentation.common.mappings.Mileage.mileage

case class PrivateKeeperDetailsCompleteFormModel(mileage: Option[Int],
                                                 dateOfSale: LocalDate,
                                                 consent: String)

object PrivateKeeperDetailsCompleteFormModel {
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsCompleteFormModel]
  final val PrivateKeeperDetailsCompleteCacheKey = "privateKeeperDetailsComplete"
  implicit val Key = CacheKey[PrivateKeeperDetailsCompleteFormModel](PrivateKeeperDetailsCompleteCacheKey)

  object Form {
    final val MileageId = "privatekeeper_mileage"
    final val DateOfSaleId = "dateofsale"
    final val TodaysDateId = "todays_date"
    final val ConsentId = "consent"

    final val Mapping = mapping(
      MileageId -> mileage,
      DateOfSaleId -> dateMapping.verifying(notInTheFuture()),
      ConsentId -> consent
    )(PrivateKeeperDetailsCompleteFormModel.apply)(PrivateKeeperDetailsCompleteFormModel.unapply)
  }
}