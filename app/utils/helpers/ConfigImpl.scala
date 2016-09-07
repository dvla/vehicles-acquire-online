package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.{ConfigProperties => VPCConfig}
import VPCConfig.{booleanProp, intProp, longProp, stringProp}
import VPCConfig.{getDurationProperty, getIntListProperty, getOptionalProperty, getProperty, getStringListProperty}
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From

class ConfigImpl extends Config {

  // Prototype message in html
  override val isPrototypeBannerVisible: Boolean = getProperty[Boolean]("prototype.disclaimer")

  // Google analytics
  override val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.acquire")

  override val isHtml5ValidationEnabled: Boolean = getProperty[Boolean]("html5Validation.enabled")

  override val startUrl: String = getProperty[String]("start.page")

  override val acquire = new AcquireConfig()

  // Opening and closing times
  override val openingTimeMinOfDay: Int = getProperty[Int]("openingTimeMinOfDay")
  override val closingTimeMinOfDay: Int = getProperty[Int]("closingTimeMinOfDay")
  override val closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins")
    .getOrElse(ConfigImpl.DefaultClosingWarnPeriodMins)
  override val closedDays: List[Int] = getIntListProperty("closedDays").getOrElse(List())

  // Web headers
  override val applicationCode: String = getProperty[String]("webHeader.applicationCode")
  override val vssServiceTypeCode: String = getProperty[String]("webHeader.vssServiceTypeCode")
  override val dmsServiceTypeCode: String = getProperty[String]("webHeader.dmsServiceTypeCode")
  override val orgBusinessUnit: String = getProperty[String]("webHeader.orgBusinessUnit")
  override val channelCode: String = getProperty[String]("webHeader.channelCode")
  override val contactId: Long = getProperty[Long]("webHeader.contactId")

  override val emailServiceMicroServiceUrlBase: String = getProperty[String]("emailServiceMicroServiceUrlBase")
  override val emailServiceMsRequestTimeout: Int =
    getOptionalProperty[Int]("emailService.ms.requesttimeout").getOrElse(ConfigImpl.DefaultMsTimeoutMillis)

  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    From(getProperty[String]("email.senderAddress"), "DO-NOT-REPLY"),
    From(getProperty[String]("email.feedbackAddress"), "Feedback"),
    getStringListProperty("email.whitelist")
  )

  override val imagesPath: String = getProperty[String]("email.image.path")

  override def assetsUrl: Option[String] = getOptionalProperty[String]("assets.url")

  override val surveyUrl: Option[String] = getOptionalProperty[String]("survey.url")
  override val surveyInterval: Long = getDurationProperty("survey.interval")
}

object ConfigImpl {
  final val DefaultMsTimeoutMillis = 10000
  final val DefaultClosingWarnPeriodMins = 15
}