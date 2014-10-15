package models

import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey

final case class VehicleTaxOrSornFormModel(temp: Option[String])

object VehicleTaxOrSornFormModel {
  implicit val JsonFormat = Json.format[VehicleTaxOrSornFormModel]
  final val VehicleTaxOrSornCacheKey = "vehicleTaxOrSorn"
  implicit val Key = CacheKey[VehicleTaxOrSornFormModel](VehicleTaxOrSornCacheKey)

  object Form {
    final val TempId = "temp"

    final val Mapping = mapping(
      TempId -> optional(text)
    )(VehicleTaxOrSornFormModel.apply)(VehicleTaxOrSornFormModel.unapply)
  }
}
