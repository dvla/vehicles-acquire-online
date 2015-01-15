package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import uk.gov.dvla.vehicles.presentation.common.services.SEND.{From, EmailConfiguration}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireConfig

class Config {

  private val notFound = "NOT FOUND"

  // Prototype message in html
  lazy val isPrototypeBannerVisible: Boolean = getProperty[Boolean]("prototype.disclaimer")

  // Google analytics
  lazy val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.acquire")

  // Progress step indicator
  lazy val isProgressBarEnabled: Boolean = getProperty[Boolean]("progressBar.enabled")

  lazy val isHtml5ValidationEnabled: Boolean = getProperty[Boolean]("html5Validation.enabled")

  lazy val startUrl: String = getProperty[String]("start.page")

  lazy val acquire = new AcquireConfig()

  lazy val ordnanceSurveyUseUprn: Boolean = getProperty[Boolean]("ordnancesurvey.useUprn")
  
  // opening and closing times
  lazy val opening: Int = getProperty[Int]("openingTime")
  lazy val closing: Int = getProperty[Int]("closingTime")

  // Web headers
  lazy val applicationCode: String = getProperty[String]("webHeader.applicationCode")
  lazy val serviceTypeCode: String = getProperty[String]("webHeader.serviceTypeCode")
  lazy val orgBusinessUnit: String = getProperty[String]("webHeader.orgBusinessUnit")


  lazy val emailConfiguration: EmailConfiguration = EmailConfiguration(
    getProperty[String]("smtp.host"),
    getProperty[Int]("smtp.port"),
    getProperty[String]("smtp.user"),
    getProperty[String]("smtp.password"),
    From(getProperty[String]("email.senderAddress"), "DO-NOT-REPLY"),
    From(getProperty[String]("email.feedbackAddress"), "Feedback"),
    getStringListProperty("email.whitelist")
  )
}
