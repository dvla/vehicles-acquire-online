package models

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{VehicleAndKeeperDetailsModel, TraderDetailsModel}

final case class AcquireCompletionViewModel(vehicleDetails: VehicleAndKeeperDetailsModel,
                                            traderDetails: TraderDetailsModel,
                                            newKeeperDetails: NewKeeperDetailsViewModel,
                                            completeAndConfirmDetails: CompleteAndConfirmFormModel,
                                            vehicleSorn: VehicleTaxOrSornFormModel,
                                            completeAndConfirmResponseModel: CompleteAndConfirmResponseModel)

object AcquireCompletionViewModel {
  implicit val JsonFormat = Json.format[CompleteAndConfirmResponseModel]
  final val AcquireCompletionCacheKey = s"${CookiePrefix}acquireCompletion"
  implicit val Key = CacheKey[CompleteAndConfirmResponseModel](AcquireCompletionCacheKey)
}
