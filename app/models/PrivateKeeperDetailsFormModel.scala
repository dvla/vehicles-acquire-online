package models

import mappings.DropDown.titleDropDown
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import uk.gov.dvla.vehicles.presentation.common.mappings.DriverNumber.driverNumber
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._

case class PrivateKeeperDetailsFormModel(title: String, firstName: String, lastName: String, email: Option[String], driverNumber: Option[String])

object PrivateKeeperDetailsFormModel {
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsFormModel]
  final val PrivateKeeperDetailsCacheKey = "privateKeeperDetails"
  implicit val Key = CacheKey[PrivateKeeperDetailsFormModel](PrivateKeeperDetailsCacheKey)

  object Form {
    final val TitleId = "privatekeeper_title"
    final val FirstNameId = "privatekeeper_firstname"
    final val LastNameId = "privatekeeper_lastname"
    final val EmailId = "privatekeeper_email"
    final val ConsentId = "consent"
    final val DriverNumberId = "privatekeeper_drivernumber"
    final val DriverNumberMaxLength = 16
    final val FirstNameMinLength = 1
    final val FirstNameMaxLength = 25
    final val LastNameMinLength = 1
    final val LastNameMaxLength = 25

    def firstNameMapping: Mapping[String] =
      nonEmptyTextWithTransform(_.trim)(FirstNameMinLength, FirstNameMaxLength) verifying validFirstName

    def validFirstName: Constraint[String] = pattern(
      regex = """^[a-zA-Z0-9\s\-\"\,\.\']{1,}$""".r,
      name = "constraint.validFirstName",
      error = "error.validFirstName")

    def lastNameMapping: Mapping[String] =
      nonEmptyTextWithTransform(_.trim)(LastNameMinLength, LastNameMaxLength) verifying validLastName

    def validLastName: Constraint[String] = pattern(
      regex = """^[a-zA-Z0-9\s\-\"\,\.\']{1,}$""".r,
      name = "constraint.validLastName",
      error = "error.validLastName")

    val titleOptions = Seq(
      ("firstOption", "Mr"),
      ("secondOption", "Mrs"),
      ("thirdOption", "Miss"),
      ("fourthOption", "Other")
    )

    final val Mapping = mapping(
      TitleId -> titleDropDown(titleOptions),
      FirstNameId -> firstNameMapping,
      LastNameId -> lastNameMapping,
      EmailId -> optional(email),
      DriverNumberId -> optional(driverNumber)
    )(PrivateKeeperDetailsFormModel.apply)(PrivateKeeperDetailsFormModel.unapply)
  }
}
