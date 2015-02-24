import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.NewKeeperEnterAddressManuallyFormModel.NewKeeperEnterAddressManuallyCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import uk.gov.dvla.vehicles.presentation.common
import common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import common.model.BusinessKeeperDetailsFormModel.businessKeeperDetailsCacheKey
import common.model.PrivateKeeperDetailsFormModel.privateKeeperDetailsCacheKey
import common.model.NewKeeperChooseYourAddressFormModel.newKeeperChooseYourAddressCacheKey
import common.model.NewKeeperDetailsViewModel.newKeeperDetailsCacheKey
import common.model.SetupTradeDetailsFormModel.setupTradeDetailsCacheKey
import common.model.TraderDetailsModel.TraderDetailsCacheKey
import common.model.VehicleAndKeeperDetailsModel.VehicleAndKeeperLookupDetailsCacheKey

package object models {
  
  final val HelpCacheKey = s"${CookiePrefix}help"
  final val SeenCookieMessageCacheKey = "seen_cookie_message" // Same value across all exemplars

  final val AcquireCacheKeys = Set(
    newKeeperChooseYourAddressCacheKey,
    bruteForcePreventionViewModelCacheKey
  )

  final val TraderDetailsCacheKeys = Set(
    setupTradeDetailsCacheKey,
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
    privateKeeperDetailsCacheKey,
    CompleteAndConfirmCacheKey,
    newKeeperChooseYourAddressCacheKey,
    NewKeeperEnterAddressManuallyCacheKey
  )

  // Set of cookies related to all data entered for a business keeper
  final val BusinessKeeperDetailsCacheKeys = Set(
    businessKeeperDetailsCacheKey,
    newKeeperChooseYourAddressCacheKey,
    NewKeeperEnterAddressManuallyCacheKey
  )

  final val CompletionCacheKeys = Set(
    VehicleTaxOrSornCacheKey,
    newKeeperDetailsCacheKey,
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
