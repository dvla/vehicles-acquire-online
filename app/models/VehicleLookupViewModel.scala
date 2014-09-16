package models

import play.api.data.Form

case class VehicleLookupViewModel(form: Form[models.VehicleLookupFormViewModel],
                                  traderName: String,
                                  address: Seq[String])
