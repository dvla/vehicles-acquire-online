package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupConfig
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireConfig

trait Config extends VehicleLookupConfig {
  
  def assetsUrl: Option[String]

  // Prototype message in html
  def isPrototypeBannerVisible: Boolean

  // Google analytics
  def googleAnalyticsTrackingId: Option[String]

  // Progress step indicator
  def isProgressBarEnabled: Boolean

  def isHtml5ValidationEnabled: Boolean

  def startUrl: String

  def acquire: AcquireConfig

  def ordnanceSurveyUseUprn: Boolean
  
  // opening and closing times
  def opening: Int
  def closing: Int

  def emailConfiguration: EmailConfiguration
}
