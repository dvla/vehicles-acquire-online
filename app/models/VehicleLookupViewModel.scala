package models

import play.api.data.Form

case class VehicleLookupViewModel(form: Form[models.VehicleLookupFormModel],
                                  traderName: String,
                                  address: Seq[String],
                                  email: Option[String])
