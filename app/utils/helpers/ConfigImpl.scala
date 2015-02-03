package utils.helpers

import uk.gov.dvla.vehicles.presentation.common
import common.ConfigProperties.{getOptionalProperty, booleanProp, intProp, stringProp, longProp}
import common.ConfigProperties.getProperty
import common.ConfigProperties.getStringListProperty
import common.services.SEND.{EmailConfiguration, From}
import common.webserviceclients.acquire.AcquireConfig

class ConfigImpl extends Config {

  private val notFound = "NOT FOUND"

  // Prototype message in html
  override val isPrototypeBannerVisible: Boolean = getProperty[Boolean]("prototype.disclaimer")

  // Google analytics
  override val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.acquire")

  // Progress step indicator
  override val isProgressBarEnabled: Boolean = getProperty[Boolean]("progressBar.enabled")

  override val isHtml5ValidationEnabled: Boolean = getProperty[Boolean]("html5Validation.enabled")

  override val startUrl: String = getProperty[String]("start.page")

  override val acquire = new AcquireConfig()

  override val ordnanceSurveyUseUprn: Boolean = getProperty[Boolean]("ordnancesurvey.useUprn")

  // opening and closing times
  override val opening: Int = getProperty[Int]("openingTime")
  override val closing: Int = getProperty[Int]("closingTime")

  // Web headers
  override val applicationCode: String = getProperty[String]("webHeader.applicationCode")
  override val serviceTypeCode: String = getProperty[String]("webHeader.serviceTypeCode")
  override val orgBusinessUnit: String = getProperty[String]("webHeader.orgBusinessUnit")
  override val channelCode: String = getProperty[String]("webHeader.channelCode")
  override val contactId: Long = getProperty[Long]("webHeader.contactId")


  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    getProperty[String]("smtp.host"),
    getProperty[Int]("smtp.port"),
    getProperty[String]("smtp.user"),
    getProperty[String]("smtp.password"),
    From(getProperty[String]("email.senderAddress"), "DO-NOT-REPLY"),
    From(getProperty[String]("email.feedbackAddress"), "Feedback"),
    getStringListProperty("email.whitelist")
  )
}
