package models

import org.joda.time.LocalDate
import play.api.data.Forms
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.DateOfBirth.optionalMapping
import uk.gov.dvla.vehicles.presentation.common.mappings.DateOfBirth.mapping
import uk.gov.dvla.vehicles.presentation.common.mappings.Mileage.mileage
import mappings.Consent.consent

case class PrivateKeeperDetailsCompleteFormModel(dateOfBirth: Option[LocalDate], mileage: Option[Int], dateOfSale: LocalDate, consent: String)

object PrivateKeeperDetailsCompleteFormModel {
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsCompleteFormModel]
  final val PrivateKeeperDetailsCompleteCacheKey = "privateKeeperDetailsComplete"
  implicit val Key = CacheKey[PrivateKeeperDetailsCompleteFormModel](PrivateKeeperDetailsCompleteCacheKey)

  object Form {
    final val DateOfBirthId = "privatekeeper_dateofbirth"
    final val MileageId = "privatekeeper_mileage"
    final val DateOfSaleId = "privatekeeper_dateofacquisition"
    final val ConsentId = "consent"

    final val Mapping = Forms.mapping(
      DateOfBirthId -> optionalMapping,
      MileageId -> mileage,
      DateOfSaleId -> mapping,
      ConsentId -> consent
    )(PrivateKeeperDetailsCompleteFormModel.apply)(PrivateKeeperDetailsCompleteFormModel.unapply)
  }
}