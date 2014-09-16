import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel.BruteForcePreventionViewModelCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.TraderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel.VehicleLookupDetailsCacheKey
import models.VehicleLookupFormViewModel.VehicleLookupFormModelCacheKey
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.SetupTradeDetailsViewModel.SetupTradeDetailsCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.PrivateKeeperDetailsViewModel.PrivateKeeperDetailsCacheKey
import models.BusinessKeeperDetailsFormModel.BusinessKeeperDetailsCacheKey

package object models {
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
    PrivateKeeperDetailsCacheKey,
    BusinessKeeperDetailsCacheKey
  )

  // The full set of cache keys. These are removed at the start of the process in the "before_you_start" page
  final val AllCacheKeys = AcquireCacheKeys
    .++(Set(HelpCacheKey))
}
