package models

import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common
import common.model.VehicleAndKeeperDetailsModel

case class BusinessKeeperDetailsViewModel(form: Form[models.BusinessKeeperDetailsFormModel],
                                          vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel)