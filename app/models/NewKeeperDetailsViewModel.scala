package models

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, AddressModel}

final case class NewKeeperDetailsModel(newKeeperName: String, newKeeperAddress: AddressModel)

/**
 * Current serving as both a view-model and model. Needs splitting.
 */
object NewKeeperDetailsModel {
  implicit val JsonFormat = Json.format[NewKeeperDetailsModel]
  final val NewKeeperDetailsCacheKey = "newKeeperDetails"
  implicit val Key = CacheKey[NewKeeperDetailsModel](value = NewKeeperDetailsCacheKey)
}

