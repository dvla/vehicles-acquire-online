package models

import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.model.NewKeeperDetailsViewModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

sealed trait ErrorType
case object NoError extends ErrorType
case object NoSelection extends ErrorType
case object NoSorn extends ErrorType



case class VehicleTaxOrSornViewModel(form: Form[models.VehicleTaxOrSornFormModel],
                                     vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
                                     keeperDetails: NewKeeperDetailsViewModel,
                                     errorType: ErrorType = NoError)

