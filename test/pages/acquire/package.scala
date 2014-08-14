package pages

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty

package object acquire {
  final val basePath = getProperty("base.path", default = "NOT-FOUND")
}
