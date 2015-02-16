package models

import uk.gov.dvla.vehicles.presentation.common.model.CacheKeyPrefix

object AcquireCacheKeyPrefix {

  implicit final val CookiePrefix = CacheKeyPrefix("acq-")
}
