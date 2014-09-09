package viewmodels

import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey
import common.mappings.BusinessName.businessNameMapping

final case class BusinessKeeperDetailsFormViewModel(businessName: String)

object BusinessKeeperDetailsFormViewModel {
  implicit val JsonFormat = Json.format[BusinessKeeperDetailsFormViewModel]
  final val BusinessKeeperDetailsCacheKey = "businessKeeperDetails"
  implicit val Key = CacheKey[BusinessKeeperDetailsFormViewModel](BusinessKeeperDetailsCacheKey)

  object Form {
    final val BusinessNameId = "businessName"

    final val Mapping = mapping(
      BusinessNameId -> businessNameMapping
    )(BusinessKeeperDetailsFormViewModel.apply)(BusinessKeeperDetailsFormViewModel.unapply)
  }
}