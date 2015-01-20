package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.{OrdnanceSurveyConfig, VehicleAndKeeperLookupConfig}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.AcquireConfig

class Version @Inject()(vehicleAndKeeperLookupConfig: VehicleAndKeeperLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        acquireConfig: AcquireConfig)
  extends controllers.Version(
    osAddressLookupConfig.baseUrl + "/version",
    vehicleAndKeeperLookupConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + "/version",
    acquireConfig.baseUrl + "/version"
  )
