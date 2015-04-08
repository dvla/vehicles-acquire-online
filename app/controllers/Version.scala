package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.OrdnanceSurveyConfig
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.VehicleAndKeeperLookupConfig

class Version @Inject()(vehicleAndKeeperLookupConfig: VehicleAndKeeperLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        acquireConfig: AcquireConfig)
  extends controllers.Version(
    osAddressLookupConfig.baseUrl + "/version",
    vehicleAndKeeperLookupConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + "/version",
    acquireConfig.baseUrl + "/version"
  )
