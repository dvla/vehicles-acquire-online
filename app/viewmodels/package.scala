import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.TraderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import viewmodels.VehicleLookupFormViewModel.VehicleLookupFormModelCacheKey
import viewmodels.BusinessChooseYourAddressViewModel.BusinessChooseYourAddressCacheKey
import viewmodels.SetupTradeDetailsViewModel.SetupTradeDetailsCacheKey
import viewmodels.EnterAddressManuallyViewModel.EnterAddressManuallyCacheKey
import viewmodels.BusinessKeeperDetailsFormViewModel.BusinessKeeperDetailsCacheKey

package object viewmodels {
  final val HelpCacheKey = "help"
  final val SeenCookieMessageCacheKey = "seen_cookie_message"

  final val AcquireCacheKeys = Set(
    SetupTradeDetailsCacheKey,
    TraderDetailsCacheKey,
    BusinessChooseYourAddressCacheKey,
    EnterAddressManuallyCacheKey,
    BruteForcePreventionViewModelCacheKey,
    VehicleLookupFormModelCacheKey,
    VehicleLookupDetailsCacheKey,
    BusinessKeeperDetailsCacheKey
  )

  // The full set of cache keys. These are removed at the start of the process in the "before_you_start" page
  final val AllCacheKeys = AcquireCacheKeys
    .++(Set(HelpCacheKey))
}
