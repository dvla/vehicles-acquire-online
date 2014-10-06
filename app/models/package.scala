import uk.gov.dvla.vehicles.presentation.common
import common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import common.model.TraderDetailsModel.TraderDetailsCacheKey
import common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.BusinessKeeperDetailsCompleteFormModel.BusinessKeeperDetailsCompleteCacheKey
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.SetupTradeDetailsFormModel.SetupTradeDetailsCacheKey
import models.PrivateKeeperDetailsCompleteFormModel.PrivateKeeperDetailsCompleteCacheKey
import models.PrivateKeeperDetailsFormModel.PrivateKeeperDetailsCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import models.NewKeeperChooseYourAddressFormModel.NewKeeperChooseYourAddressCacheKey

package object models {
  final val HelpCacheKey = "help"
  final val SeenCookieMessageCacheKey = "seen_cookie_message"

  final val AcquireCacheKeys = Set(
    SetupTradeDetailsCacheKey,
    TraderDetailsCacheKey,
    BusinessChooseYourAddressCacheKey,
    EnterAddressManuallyCacheKey,
    NewKeeperChooseYourAddressCacheKey,
    BruteForcePreventionViewModelCacheKey
  )

  // Set of cookies related to looking up a vehicle
  final val VehicleLookupCacheKeys = Set(
    VehicleLookupFormModelCacheKey,
    VehicleLookupDetailsCacheKey,
    VehicleLookupResponseCodeCacheKey
  )

  // Set of cookies related to all data entered for a private keeper
  final val PrivateKeeperDetailsCacheKeys = Set(
    PrivateKeeperDetailsCacheKey,
    PrivateKeeperDetailsCompleteCacheKey
  )

  // Set of cookies related to all data entered for a business keeper
  final val BusinessKeeperDetailsCacheKeys = Set(
    BusinessKeeperDetailsCacheKey,
    BusinessKeeperDetailsCompleteCacheKey
  )

  // The full set of cache keys. These are removed at the start of the process in the "before_you_start" page
  final val AllCacheKeys = AcquireCacheKeys
    .++(VehicleLookupCacheKeys)
    .++(PrivateKeeperDetailsCacheKeys)
    .++(BusinessKeeperDetailsCacheKeys)
    .++(Set(HelpCacheKey))
}
