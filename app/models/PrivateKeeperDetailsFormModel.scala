package models

import mappings.DropDown.titleDropDown
import org.joda.time.LocalDate
import play.api.data.Forms.{mapping, optional}
import play.api.data.Mapping
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints.pattern
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey
import common.mappings.Date.optionalDateOfBirth
import common.mappings.Email.email
import common.mappings.DriverNumber.driverNumber
import common.mappings.Postcode.postcode
import common.views.helpers.FormExtensions.nonEmptyTextWithTransform

case class PrivateKeeperDetailsFormModel(title: String, 
                                         firstName: String, 
                                         lastName: String,
                                         dateOfBirth: Option[LocalDate],
                                         email: Option[String], 
                                         driverNumber: Option[String],
                                         postcode: String)

object PrivateKeeperDetailsFormModel {
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsFormModel]
  final val PrivateKeeperDetailsCacheKey = "privateKeeperDetails"
  implicit val Key = CacheKey[PrivateKeeperDetailsFormModel](PrivateKeeperDetailsCacheKey)

  object Form {
    final val TitleId = "privatekeeper_title"
    final val FirstNameId = "privatekeeper_firstname"
    final val LastNameId = "privatekeeper_lastname"
    final val DateOfBirthId = "privatekeeper_dateofbirth"
    final val EmailId = "privatekeeper_email"
    final val DriverNumberId = "privatekeeper_drivernumber"
    final val PostcodeId = "privatekeeper_postcode"
    final val ConsentId = "consent"

    final val DriverNumberMaxLength = 16
    final val FirstNameMinLength = 1
    final val FirstNameMaxLength = 25
    final val LastNameMinLength = 1
    final val LastNameMaxLength = 25

    def firstNameMapping: Mapping[String] =
      nonEmptyTextWithTransform(_.trim)(FirstNameMinLength, FirstNameMaxLength) verifying validFirstName

    final val NameRegEx = """^[a-zA-Z0-9\s\-\"\,\.\']{1,}$""".r
    
    def validFirstName: Constraint[String] = pattern(
      regex = NameRegEx,
      name = "constraint.validFirstName",
      error = "error.validFirstName")

    def lastNameMapping: Mapping[String] =
      nonEmptyTextWithTransform(_.trim)(LastNameMinLength, LastNameMaxLength) verifying validLastName

    def validLastName: Constraint[String] = pattern(
      regex = NameRegEx,
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
      DateOfBirthId -> optionalDateOfBirth,
      EmailId -> optional(email),
      DriverNumberId -> optional(driverNumber),
      PostcodeId -> postcode
    )(PrivateKeeperDetailsFormModel.apply)(PrivateKeeperDetailsFormModel.unapply)
  }
}