package models

import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey
import models.AcquireCacheKeyPrefix.CookiePrefix

final case class VehicleTaxOrSornFormModel(sornVehicle: Option[String])

object VehicleTaxOrSornFormModel {
  implicit val JsonFormat = Json.format[VehicleTaxOrSornFormModel]
  final val VehicleTaxOrSornCacheKey = s"${CookiePrefix}vehicleTaxOrSorn"
  implicit val Key = CacheKey[VehicleTaxOrSornFormModel](VehicleTaxOrSornCacheKey)

  object Form {
    final val SornVehicleId = "sornVehicle"

    final val Mapping = mapping(
      SornVehicleId -> optional(text)
    )(VehicleTaxOrSornFormModel.apply)(VehicleTaxOrSornFormModel.unapply)
  }
}
