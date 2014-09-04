package viewmodels

import play.api.libs.json.Json
import play.api.data.Forms._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import mappings.DropDown.titleDropDown

case class PrivateKeeperDetailsViewModel(title: String)

object PrivateKeeperDetailsViewModel {
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsViewModel]
  final val PrivateKeeperDetailsCacheKey = "privateKeeperDetails"
  implicit val Key = CacheKey[PrivateKeeperDetailsViewModel](PrivateKeeperDetailsCacheKey)

  object Form {
    final val TitleId = "privatekeeper_title"

    val titleOptions = Seq(
      ("firstOption", "Mr"),
      ("secondOption", "Mrs"),
      ("thirdOption", "Ms"),
      ("fourthOption", "Other")
    )

    final val Mapping = mapping(
      TitleId -> titleDropDown(titleOptions)
    )(PrivateKeeperDetailsViewModel.apply)(PrivateKeeperDetailsViewModel.unapply)
  }
}