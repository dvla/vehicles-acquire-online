package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.controllers.VehicleLookupConfig
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireConfig
import uk.gov.dvla.vehicles.presentation.common.utils.helpers.CommonConfig

trait Config extends VehicleLookupConfig with CommonConfig {
  
  def assetsUrl: Option[String]

  // Google analytics
  def googleAnalyticsTrackingId: Option[String]

  def isHtml5ValidationEnabled: Boolean

  def startUrl: String

  def acquire: AcquireConfig

  // Opening and closing times
  def openingTimeMinOfDay: Int
  def closingTimeMinOfDay: Int
  def closingWarnPeriodMins: Int
  def closedDays: List[Int]

  def emailServiceMicroServiceUrlBase: String
  def emailServiceMsRequestTimeout: Int

  def emailConfiguration: EmailConfiguration

  def imagesPath: String

  // Survey URL
  def surveyUrl: Option[String]
  def surveyInterval: Long
}
