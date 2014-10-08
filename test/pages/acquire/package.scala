package pages

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty

package object acquire {
  private final val applicationContext = getProperty("application.context", default = "/")

  def buildAppUrl(urlPart: String) = {
    val appContextWithSlash = if (!applicationContext.endsWith("/")) s"$applicationContext/" else applicationContext
    val urlPartWithoutSlash = urlPart.dropWhile(_ == '/')

    appContextWithSlash + urlPartWithoutSlash
  }
}
