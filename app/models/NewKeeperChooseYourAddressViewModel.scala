package models

import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common
import common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.k2kandacquire.models

case class NewKeeperChooseYourAddressViewModel(form: Form[models.NewKeeperChooseYourAddressFormModel],
                                               vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel)