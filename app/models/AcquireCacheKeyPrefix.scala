package models

import uk.gov.dvla.vehicles.presentation.common.model.CacheKeyPrefix

/**
 * Created by gerasimosarvanitis on 11/02/2015.
 */
object AcquireCacheKeyPrefix {

  implicit final val CookiePrefix = CacheKeyPrefix("acq-")

}
