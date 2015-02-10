import uk.gov.dvla.vehicles.presentation.common
import common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import common.model.TraderDetailsModel.TraderDetailsCacheKey
import common.model.VehicleAndKeeperDetailsModel.VehicleAndKeeperLookupDetailsCacheKey
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.SetupTradeDetailsFormModel.SetupTradeDetailsCacheKey
import models.CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.NewKeeperChooseYourAddressFormModel.NewKeeperChooseYourAddressCacheKey
import models.NewKeeperEnterAddressManuallyFormModel.NewKeeperEnterAddressManuallyCacheKey
import models.NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey

package object models {
  final val CacheKeyPrefix = "acq-"
  final val HelpCacheKey = s"${CacheKeyPrefix}help"
  final val SeenCookieMessageCacheKey = "seen_cookie_message" // Same value across all exemplars

  final val AcquireCacheKeys = Set(
    NewKeeperChooseYourAddressCacheKey,
    BruteForcePreventionViewModelCacheKey
  )

  final val TraderDetailsCacheKeys = Set(
    SetupTradeDetailsCacheKey,
    TraderDetailsCacheKey,
    BusinessChooseYourAddressCacheKey,
    EnterAddressManuallyCacheKey
  )

  // Set of cookies related to looking up a vehicle
  final val VehicleLookupCacheKeys = Set(
    VehicleLookupFormModelCacheKey,
    VehicleAndKeeperLookupDetailsCacheKey,
    VehicleLookupResponseCodeCacheKey
  )

  // Set of cookies related to all data entered for a private keeper
  final val PrivateKeeperDetailsCacheKeys = Set(
    PrivateKeeperDetailsCacheKey,
    CompleteAndConfirmCacheKey,
    NewKeeperChooseYourAddressCacheKey,
    NewKeeperEnterAddressManuallyCacheKey
  )

  // Set of cookies related to all data entered for a business keeper
  final val BusinessKeeperDetailsCacheKeys = Set(
    BusinessKeeperDetailsCacheKey,
    NewKeeperChooseYourAddressCacheKey,
    NewKeeperEnterAddressManuallyCacheKey
  )

  final val CompletionCacheKeys = Set(
    VehicleTaxOrSornCacheKey,
    NewKeeperDetailsCacheKey,
    CompleteAndConfirmCacheKey,
    VehicleLookupResponseCodeCacheKey,
    AcquireCompletionResponseCacheKey
  )

  // Vehicle, new keeper and completion cache keys are removed. Trader cache keys remain
  final val VehicleNewKeeperCompletionCacheKeys =
    AcquireCacheKeys
    .++(VehicleLookupCacheKeys)
    .++(PrivateKeeperDetailsCacheKeys)
    .++(BusinessKeeperDetailsCacheKeys)
    .++(CompletionCacheKeys)
    .++(Set(HelpCacheKey))

  // The full set of cache keys. These are removed at the start of the process in the "before_you_start" page
  final val AllCacheKeys =
    VehicleNewKeeperCompletionCacheKeys
    .++(TraderDetailsCacheKeys)
}