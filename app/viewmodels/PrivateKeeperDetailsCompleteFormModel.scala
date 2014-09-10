package viewmodels

import org.joda.time.LocalDate
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.DateOfBirth.optionalMapping

case class PrivateKeeperDetailsCompleteFormModel(dateOfBirth: Option[LocalDate])

object PrivateKeeperDetailsCompleteFormModel {
//  implicit val DateOfBirthJsonFormat = Json.format[DateOfBirth]
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsCompleteFormModel]
  final val PrivateKeeperDetailsCompleteCacheKey = "privateKeeperDetailsComplete"
  implicit val Key = CacheKey[PrivateKeeperDetailsCompleteFormModel](PrivateKeeperDetailsCompleteCacheKey)

  object Form {
    final val DateOfBirthId = "privatekeeper_dateofbirth"

    final val Mapping = mapping(
      DateOfBirthId -> optionalMapping
    )(PrivateKeeperDetailsCompleteFormModel.apply)(PrivateKeeperDetailsCompleteFormModel.unapply)
  }
}
