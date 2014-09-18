package models

import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Mileage.mileage

case class BusinessKeeperDetailsCompleteFormModel(mileage: Option[Int])

object BusinessKeeperDetailsCompleteFormModel {
  implicit val JsonFormat = Json.format[BusinessKeeperDetailsCompleteFormModel]
  final val BusinessKeeperDetailsCompleteCacheKey = "businessKeeperDetailsComplete"
  implicit val Key = CacheKey[BusinessKeeperDetailsCompleteFormModel](BusinessKeeperDetailsCompleteCacheKey)

  object Form {
    final val DateOfBirthId = "businesskeeper_dateofbirth"
    final val MileageId = "businesskeeper_mileage"

    final val Mapping = mapping(
      MileageId -> mileage
    )(BusinessKeeperDetailsCompleteFormModel.apply)(BusinessKeeperDetailsCompleteFormModel.unapply)
  }
}
