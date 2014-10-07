package models

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

final case class NewKeeperDetailsViewModel(newKeeperName: String, newKeeperAddress: AddressModel)

/**
 * Current serving as both a view-model and model. Needs splitting.
 */
object NewKeeperDetailsViewModel {
  implicit val JsonFormat = Json.format[NewKeeperDetailsViewModel]
  final val NewKeeperDetailsCacheKey = "newKeeperDetails"
  implicit val Key = CacheKey[NewKeeperDetailsViewModel](value = NewKeeperDetailsCacheKey)
}

