package models

import uk.gov.dvla.vehicles.presentation.common.model.{TraderDetailsModel, VehicleDetailsModel}

final case class AcquireSuccessViewModel(vehicleDetails: VehicleDetailsModel,
                                         traderDetails: TraderDetailsModel,
                                         newKeeperDetails: NewKeeperDetailsViewModel,
                                         completeAndConfirmDetails: CompleteAndConfirmFormModel) {

}