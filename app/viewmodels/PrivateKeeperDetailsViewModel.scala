package viewmodels

import mappings.DropDown.titleDropDown
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._

case class PrivateKeeperDetailsViewModel(title: String, firstName: String, email: Option[String])

object PrivateKeeperDetailsViewModel {
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsViewModel]
  final val PrivateKeeperDetailsCacheKey = "privateKeeperDetails"
  implicit val Key = CacheKey[PrivateKeeperDetailsViewModel](PrivateKeeperDetailsCacheKey)

  object Form {
    final val TitleId = "privatekeeper_title"
    final val EmailId = "privatekeeper_email"
    final val FirstNameId = "privatekeeper_firstname"
    final val FirstNameMinLength = 1
    final val FirstNameMaxLength = 25

    def firstNameMapping: Mapping[String] =
      nonEmptyTextWithTransform(_.trim)(FirstNameMinLength, FirstNameMaxLength) verifying validFirstName

    def validFirstName: Constraint[String] = pattern(
      regex = """^[a-zA-Z0-9\s\-\"\,\.\']{1,}$""".r,
      name = "constraint.validFirstName",
      error = "error.validFirstName")

    val titleOptions = Seq(
      ("firstOption", "Mr"),
      ("secondOption", "Mrs"),
      ("thirdOption", "Miss"),
      ("fourthOption", "Other")
    )

    final val Mapping = mapping(
      TitleId -> titleDropDown(titleOptions),
      FirstNameId -> firstNameMapping,
      EmailId -> optional(email)
    )(PrivateKeeperDetailsViewModel.apply)(PrivateKeeperDetailsViewModel.unapply)
  }
}