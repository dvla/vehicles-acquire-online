package models

import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common
import common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.model

case class NewKeeperChooseYourAddressViewModel(form: Form[model.NewKeeperChooseYourAddressFormModel],
                                               vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel)