package controllers.acquire

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionConfig
import scala.concurrent.duration.DurationInt

/**
 * Created by gerasimosarvanitis on 09/01/2015.
 */
class TestBruteForcePreventionConfig extends BruteForcePreventionConfig {

  //  val baseUrl: String = getProperty("bruteForcePrevention.baseUrl", "NOT FOUND")
  //  val requestTimeoutMillis: Int = getProperty("bruteForcePrevention.requestTimeout", 5.seconds.toMillis.toInt)
  //  val isEnabled: Boolean = getProperty("bruteForcePrevention.enabled", default = true)
  //  val nameHeader: String = getProperty("bruteForcePrevention.headers.serviceName", "")
  //  val maxAttemptsHeader: Int = getProperty("bruteForcePrevention.headers.maxAttempts", 3)
  //  val expiryHeader: String = getProperty("bruteForcePrevention.headers.expiry", "")

  override lazy val baseUrl: String =  "NOT FOUND"
  override lazy val requestTimeoutMillis: Int = 5.seconds.toMillis.toInt
  override lazy val isEnabled: Boolean = true
  override lazy val nameHeader: String = ""
  override lazy val maxAttemptsHeader: Int = 3
  override lazy val expiryHeader: String = ""

}
