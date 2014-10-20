package controllers.acquire

import controllers.VehicleTaxOrSorn
import helpers.UnitSpec
import models.VehicleTaxOrSornFormModel.Form.SornVehicleId

final class VehicleTaxOrSornFormSpec extends UnitSpec {

  final val SornSelected = "true"
  final val SornNotSelected = ""

  "form" should {
    "accept when sorn is selected" in {
      formWithValidDefaults().get.sornVehicle should equal(Some("true"))
    }

    "accept when sorn is not selected" in {
      formWithValidDefaults(sornVehicle = SornNotSelected).get.sornVehicle should equal(None)
    }
  }

  private def formWithValidDefaults(sornVehicle: String = SornSelected) = {
    injector.getInstance(classOf[VehicleTaxOrSorn])
      .form.bind(
        Map(
          SornVehicleId -> sornVehicle
        )
      )
  }
}
