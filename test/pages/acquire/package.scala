package pages

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty

package object acquire {
  final val applicationContext = getProperty("application.context", default = "")
}
