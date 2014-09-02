package viewmodels

import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.{VehicleRegistrationNumber, DocumentReferenceNumber}

final case class VehicleLookupFormViewModel(referenceNumber: String,
                                        registrationNumber: String,
                                        vehicleSoldTo: String)

object VehicleLookupFormViewModel {
  implicit val JsonFormat = Json.format[VehicleLookupFormViewModel]
  final val VehicleLookupFormModelCacheKey = "vehicleLookupFormModel"
  implicit val Key = CacheKey[VehicleLookupFormViewModel](VehicleLookupFormModelCacheKey)
  final val VehicleLookupResponseCodeCacheKey = "vehicleLookupResponseCode"

  object Form {
    final val DocumentReferenceNumberId = "documentReferenceNumber"
    final val VehicleRegistrationNumberId = "vehicleRegistrationNumber"
    final val VehicleSoldToId = "vehicleSoldTo"

    final val Mapping = mapping(
      DocumentReferenceNumberId -> DocumentReferenceNumber.referenceNumber,
      VehicleRegistrationNumberId -> VehicleRegistrationNumber.registrationNumber,
      VehicleSoldToId -> nonEmptyText
    )(VehicleLookupFormViewModel.apply)(VehicleLookupFormViewModel.unapply)
  }
}
