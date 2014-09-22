package models

import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Mileage.mileage
import mappings.Consent.consent

case class BusinessKeeperDetailsCompleteFormModel(mileage: Option[Int], consent: String)

object BusinessKeeperDetailsCompleteFormModel {
  implicit val JsonFormat = Json.format[BusinessKeeperDetailsCompleteFormModel]
  final val BusinessKeeperDetailsCompleteCacheKey = "businessKeeperDetailsComplete"
  implicit val Key = CacheKey[BusinessKeeperDetailsCompleteFormModel](BusinessKeeperDetailsCompleteCacheKey)

  object Form {
    final val DateOfBirthId = "businesskeeper_dateofbirth"
    final val MileageId = "businesskeeper_mileage"
    final val ConsentId = "consent"

    final val Mapping = mapping(
      MileageId -> mileage,
      ConsentId -> consent
    )(BusinessKeeperDetailsCompleteFormModel.apply)(BusinessKeeperDetailsCompleteFormModel.unapply)
  }
}
