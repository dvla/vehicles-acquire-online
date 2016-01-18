import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.CompleteAndConfirmFormModel.CompleteAndConfirmCacheKey
import models.CompleteAndConfirmResponseModel.AcquireCompletionResponseCacheKey
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import models.VehicleLookupFormModel.VehicleLookupFormModelCacheKey
import models.VehicleLookupFormModel.VehicleLookupResponseCodeCacheKey
import models.VehicleTaxOrSornFormModel.VehicleTaxOrSornCacheKey
import uk.gov.dvla.vehicles.presentation.common
import common.model.BruteForcePreventionModel.bruteForcePreventionViewModelCacheKey
import common.model.BusinessKeeperDetailsFormModel.businessKeeperDetailsCacheKey
import common.model.PrivateKeeperDetailsFormModel.privateKeeperDetailsCacheKey
import common.model.NewKeeperChooseYourAddressFormModel.newKeeperChooseYourAddressCacheKey
import common.model.NewKeeperEnterAddressManuallyFormModel.newKeeperEnterAddressManuallyCacheKey
import common.model.NewKeeperDetailsViewModel.newKeeperDetailsCacheKey
import common.model.SetupTradeDetailsFormModel.setupTradeDetailsCacheKey
import common.model.TraderDetailsModel.traderDetailsCacheKey
import common.model.VehicleAndKeeperDetailsModel.vehicleAndKeeperLookupDetailsCacheKey

package object models {

  final val SurveyRequestTriggerDateCacheKey = s"${CookiePrefix}surveyRequestTriggerDate"
  final val IdentifierCacheKey = s"${CookiePrefix}identifier"

  final val AcquireCacheKeys = Set(
    newKeeperChooseYourAddressCacheKey,
    bruteForcePreventionViewModelCacheKey
  )

  final val TraderDetailsCacheKeys = Set(
    setupTradeDetailsCacheKey,
    traderDetailsCacheKey,
    BusinessChooseYourAddressCacheKey,
    EnterAddressManuallyCacheKey
  )

  // Set of cookies related to looking up a vehicle
  final val VehicleLookupCacheKeys = Set(
    VehicleLookupFormModelCacheKey,
    vehicleAndKeeperLookupDetailsCacheKey,
    VehicleLookupResponseCodeCacheKey
  )

  // Set of cookies related to all data entered for a private keeper
  final val PrivateKeeperDetailsCacheKeys = Set(
    privateKeeperDetailsCacheKey,
    CompleteAndConfirmCacheKey,
    newKeeperChooseYourAddressCacheKey,
    newKeeperEnterAddressManuallyCacheKey
  )

  // Set of cookies related to all data entered for a business keeper
  final val BusinessKeeperDetailsCacheKeys = Set(
    businessKeeperDetailsCacheKey,
    newKeeperChooseYourAddressCacheKey,
    newKeeperEnterAddressManuallyCacheKey
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

  // The full set of cache keys. These are removed at the start of the process in the "before_you_start" page
  final val AllCacheKeys =
    VehicleNewKeeperCompletionCacheKeys
    .++(TraderDetailsCacheKeys)
    .+(IdentifierCacheKey)
}
