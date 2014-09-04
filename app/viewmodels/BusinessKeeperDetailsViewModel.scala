package viewmodels

import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common
import common.model.VehicleDetailsModel

/*
  The form supports data entry via form submission
  The vehicleDetails contains data for display
 */
case class BusinessKeeperDetailsViewModel(form: Form[viewmodels.BusinessKeeperDetailsFormViewModel],
                                  vehicleDetails: VehicleDetailsModel)
