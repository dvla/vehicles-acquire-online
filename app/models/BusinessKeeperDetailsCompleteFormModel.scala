package models

import mappings.Consent.consent
import org.joda.time.LocalDate
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey
import common.mappings.Mileage.mileage
import common.mappings.Date.nonFutureDateMapping

case class BusinessKeeperDetailsCompleteFormModel(mileage: Option[Int], consent: String, dateOfSale: LocalDate)

object BusinessKeeperDetailsCompleteFormModel {
  implicit val JsonFormat = Json.format[BusinessKeeperDetailsCompleteFormModel]
  final val BusinessKeeperDetailsCompleteCacheKey = "businessKeeperDetailsComplete"
  implicit val Key = CacheKey[BusinessKeeperDetailsCompleteFormModel](BusinessKeeperDetailsCompleteCacheKey)

  object Form {
    final val MileageId = "businesskeeper_mileage"
    final val DateOfSaleId = "businesskeeper_dateofsale"
    final val ConsentId = "consent"

    final val Mapping = mapping(
      MileageId -> mileage,
      ConsentId -> consent,
      DateOfSaleId -> nonFutureDateMapping
    )(BusinessKeeperDetailsCompleteFormModel.apply)(BusinessKeeperDetailsCompleteFormModel.unapply)
  }
}
