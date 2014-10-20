package models

import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common
import common.model.VehicleDetailsModel

case class VehicleTaxOrSornViewModel(form: Form[models.VehicleTaxOrSornFormModel],
                                          vehicleDetails: VehicleDetailsModel,
                                          keeperDetails: NewKeeperDetailsViewModel)
