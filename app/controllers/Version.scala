package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.{OrdnanceSurveyConfig, VehicleLookupConfig}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireConfig

class Version @Inject()(vehicleLookupConfig: VehicleLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        acquireConfig: AcquireConfig)
  extends controllers.Version(
    osAddressLookupConfig.baseUrl + "/version",
    vehicleLookupConfig.baseUrl + "/version",
    acquireConfig.baseUrl + "/version"
  )
