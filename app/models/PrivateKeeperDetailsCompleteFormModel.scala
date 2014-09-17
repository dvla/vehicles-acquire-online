package models

import org.joda.time.LocalDate
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.DateOfBirth.optionalMapping
import uk.gov.dvla.vehicles.presentation.common.mappings.Mileage.mileage

case class PrivateKeeperDetailsCompleteFormModel(dateOfBirth: Option[LocalDate], mileage: Option[Int])

object PrivateKeeperDetailsCompleteFormModel {
//  implicit val LocalDateJsonFormat = Json.format[LocalDate]
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsCompleteFormModel]
  final val PrivateKeeperDetailsCompleteCacheKey = "privateKeeperDetailsComplete"
  implicit val Key = CacheKey[PrivateKeeperDetailsCompleteFormModel](PrivateKeeperDetailsCompleteCacheKey)

  object Form {
    final val DateOfBirthId = "privatekeeper_dateofbirth"
    final val MileageId = "privatekeeper_mileage"

    final val Mapping = mapping(
      DateOfBirthId -> optionalMapping,
      MileageId -> mileage
    )(PrivateKeeperDetailsCompleteFormModel.apply)(PrivateKeeperDetailsCompleteFormModel.unapply)
  }
}
