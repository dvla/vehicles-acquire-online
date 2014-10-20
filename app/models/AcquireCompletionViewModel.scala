package models

import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel}

final case class AcquireCompletionViewModel(vehicleDetails: VehicleDetailsModel,
                                            traderDetails: TraderDetailsModel,
                                            newKeeperDetails: NewKeeperDetailsViewModel,
                                            completeAndConfirmDetails: CompleteAndConfirmFormModel,
                                            transactionId: String,
                                            transactionTimestamp: DateTime)

object AcquireCompletionViewModel {
  implicit val JsonFormat = Json.format[AcquireCompletionViewModel]
  final val AcquireCompletionCacheKey = "acquireCompletion"
  implicit val Key = CacheKey[AcquireCompletionViewModel](AcquireCompletionCacheKey)
}