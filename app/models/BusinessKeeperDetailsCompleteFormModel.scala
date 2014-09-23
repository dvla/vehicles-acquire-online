package models

import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Mileage.mileage
import uk.gov.dvla.vehicles.presentation.common.mappings.Date.nonFutureDateMapping
import mappings.Consent.consent
import org.joda.time.LocalDate

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
