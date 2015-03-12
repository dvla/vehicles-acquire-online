package models

import play.api.data.Forms._
import play.api.data.{FormError, Mapping}
import play.api.data.format.Formatter
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey
import models.AcquireCacheKeyPrefix.CookiePrefix

final case class VehicleTaxOrSornFormModel(sornVehicle: Option[String], select: String)

object VehicleTaxOrSornFormModel {
  implicit val JsonFormat = Json.format[VehicleTaxOrSornFormModel]
  final val VehicleTaxOrSornCacheKey = s"${CookiePrefix}vehicleTaxOrSorn"
  implicit val Key = CacheKey[VehicleTaxOrSornFormModel](VehicleTaxOrSornCacheKey)

  object Form {

    final val TaxId = "tax"
    final val SornId = "sorn"
    final val NeitherId = "neither"

    final val SornVehicleId = "sornVehicle"
    final val SelectId = "select"

    final val Mapping = mapping(
      SornVehicleId -> optional(text),
      SelectId -> nonEmptyText
    )(VehicleTaxOrSornFormModel.apply)(VehicleTaxOrSornFormModel.unapply)
  }
}
