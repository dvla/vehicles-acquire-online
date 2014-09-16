package models

import mappings.FleetNumber.fleetNumberMapping
import play.api.data.Forms._
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey
import common.mappings.BusinessName.businessNameMapping
import common.mappings.Email.email

final case class BusinessKeeperDetailsFormModel(fleetNumber: Option[String], businessName: String, email: Option[String])

object BusinessKeeperDetailsFormModel {
  implicit val JsonFormat = Json.format[BusinessKeeperDetailsFormModel]
  final val BusinessKeeperDetailsCacheKey = "businessKeeperDetails"
  implicit val Key = CacheKey[BusinessKeeperDetailsFormModel](BusinessKeeperDetailsCacheKey)

  object Form {
    final val FleetNumberId = "fleetNumber"
    final val BusinessNameId = "businessName"
    final val EmailId = "email"

    final val Mapping = mapping(
      FleetNumberId -> fleetNumberMapping,
      BusinessNameId -> businessNameMapping,
      EmailId -> optional(email)
    )(BusinessKeeperDetailsFormModel.apply)(BusinessKeeperDetailsFormModel.unapply)
  }
}
