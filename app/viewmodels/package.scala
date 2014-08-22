import uk.gov.dvla.vehicles.presentation.common.model.BruteForcePreventionModel._
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel._
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel._
import viewmodels.BusinessChooseYourAddressViewModel._
import viewmodels.SetupTradeDetailsViewModel._

package object viewmodels {
  // Fill this class out as we develop the screens
  final val HelpCacheKey = "help"
  final val SeenCookieMessageCacheKey = "seen_cookie_message"


  // Set of cookies related to a single vehicle disposal. Removed once the vehicle is successfully disposed
  final val AcquireCacheKeys = Set(
    BruteForcePreventionViewModelCacheKey,
    VehicleLookupDetailsCacheKey
  )

  // Set of cookies that store the trade details data. These are retained after a successful disposal
  // so the trader does not have to re-enter their details when disposing subsequent vehicles
  final val TradeDetailsCacheKeys = Set(SetupTradeDetailsCacheKey,
    TraderDetailsCacheKey,
    BusinessChooseYourAddressCacheKey
  )

  // The full set of cache keys. These are removed at the start of the process in the "before_you_start" page
  final val AllCacheKeys = TradeDetailsCacheKeys.++(AcquireCacheKeys)
    .++(Set(HelpCacheKey))
}
