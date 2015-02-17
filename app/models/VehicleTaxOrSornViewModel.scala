package models

import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.model.NewKeeperDetailsViewModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

case class VehicleTaxOrSornViewModel(form: Form[models.VehicleTaxOrSornFormModel],
                                     vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
                                     keeperDetails: NewKeeperDetailsViewModel)

