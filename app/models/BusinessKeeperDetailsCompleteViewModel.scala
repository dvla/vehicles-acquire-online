package models

import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel

case class BusinessKeeperDetailsCompleteViewModel(form: Form[BusinessKeeperDetailsCompleteFormModel],
                                                 vehicleDetails: VehicleDetailsModel,
                                                 keeperDetails: BusinessKeeperDetailsFormModel)
