package models

import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupFormModelBase
import uk.gov.dvla.vehicles.presentation.common.mappings.{VehicleRegistrationNumber, DocumentReferenceNumber}
import models.AcquireCacheKeyPrefix.CookiePrefix

final case class VehicleLookupFormModel(referenceNumber: String,
                                        registrationNumber: String,
                                        vehicleSoldTo: String) extends VehicleLookupFormModelBase

object VehicleLookupFormModel {
  implicit val JsonFormat = Json.format[VehicleLookupFormModel]
  final val VehicleLookupFormModelCacheKey = s"${CookiePrefix}vehicleLookupFormModel"
  implicit val Key = CacheKey[VehicleLookupFormModel](VehicleLookupFormModelCacheKey)
  final val VehicleLookupResponseCodeCacheKey = s"${CookiePrefix}vehicleLookupResponseCode"

  object Form {
    final val DocumentReferenceNumberId = "documentReferenceNumber"
    final val VehicleRegistrationNumberId = "vehicleRegistrationNumber"
    final val VehicleSoldToId = "vehicleSoldTo"

    final val Mapping = mapping(
      DocumentReferenceNumberId -> DocumentReferenceNumber.referenceNumber,
      VehicleRegistrationNumberId -> VehicleRegistrationNumber.registrationNumber,
      VehicleSoldToId -> nonEmptyText
    )(VehicleLookupFormModel.apply)(VehicleLookupFormModel.unapply)
  }
}
