package viewmodels

import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel

case class PrivateKeeperDetailsCompleteViewModel(form: Form[PrivateKeeperDetailsCompleteFormModel],
                                                 vehicleDetails: VehicleDetailsModel,
                                                 keeperDetails: PrivateKeeperDetailsViewModel)
