package viewmodels

import play.api.data.Form

case class VehicleLookupViewModel(form: Form[viewmodels.VehicleLookupFormViewModel],
                                  traderName: String,
                                  address: Seq[String])
