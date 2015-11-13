package models

import models.AcquireCacheKeyPrefix.CookiePrefix
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class VehicleTaxOrSornFormModel(sornVehicle: Option[String], select: String)

object VehicleTaxOrSornFormModel {
  implicit val JsonFormat = Json.format[VehicleTaxOrSornFormModel]
  final val VehicleTaxOrSornCacheKey = s"${CookiePrefix}vehicleTaxOrSorn"
  implicit val Key = CacheKey[VehicleTaxOrSornFormModel](VehicleTaxOrSornCacheKey)

  object Form {

    final val SornFormError = "VehicleTaxOrSornFormModel"
    // these Ids must be one character since they are passed to VSS where they are limited to one character length (see vehicles-acquire-fulfil)
    final val TaxId = "T"
    final val SornId = "S"
    final val NeitherId = "N"

    final val SornVehicleId = "sornVehicle"
    final val SelectId = "select"

    final val Mapping = mapping(
      SornVehicleId -> optional(text),
      SelectId -> nonEmptyText
    )(VehicleTaxOrSornFormModel.apply)(VehicleTaxOrSornFormModel.unapply)
  }
}
