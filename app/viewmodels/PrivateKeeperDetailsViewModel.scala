package viewmodels

import play.api.libs.json.Json
import play.api.data.Forms._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import mappings.DropDown.titleDropDown
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

case class PrivateKeeperDetailsViewModel(title: String, email: Option[String])

object PrivateKeeperDetailsViewModel {
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsViewModel]
  final val PrivateKeeperDetailsCacheKey = "privateKeeperDetails"
  implicit val Key = CacheKey[PrivateKeeperDetailsViewModel](PrivateKeeperDetailsCacheKey)

  object Form {
    final val TitleId = "privatekeeper_title"
    final val EmailId = "privatekeeper_email"

    val titleOptions = Seq(
      ("firstOption", "Mr"),
      ("secondOption", "Mrs"),
      ("thirdOption", "Miss"),
      ("fourthOption", "Other")
    )

    final val Mapping = mapping(
      TitleId -> titleDropDown(titleOptions),
      EmailId -> optional(email)
    )(PrivateKeeperDetailsViewModel.apply)(PrivateKeeperDetailsViewModel.unapply)
  }
}