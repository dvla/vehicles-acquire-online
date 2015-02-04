package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getStringListProperty
import uk.gov.dvla.vehicles.presentation.common.services.SEND.{From, EmailConfiguration}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireConfig

trait Config {

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

  // Web headers
  def applicationCode: String
  def vssServiceTypeCode: String
  def dmsServiceTypeCode: String
  def orgBusinessUnit: String
  def channelCode: String
  def contactId: Long


  def emailConfiguration: EmailConfiguration
}
