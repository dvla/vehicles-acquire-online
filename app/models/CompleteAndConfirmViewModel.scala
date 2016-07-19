package models

import controllers.routes.CompleteAndConfirm
import play.api.data.Form
import play.api.mvc.Call
import uk.gov.dvla.vehicles.presentation.common.model.NewKeeperDetailsViewModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

case class CompleteAndConfirmViewModel(form: Form[CompleteAndConfirmFormModel],
                                       vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
                                       keeperDetails: NewKeeperDetailsViewModel,
                                       vehicleSorn: VehicleTaxOrSornFormModel,
                                       showDateOfSaleWarning: Boolean = false,
                                       submitAction: Call = CompleteAndConfirm.submitWithDateCheck(),
                                       disposalDate: Option[String] = None)
