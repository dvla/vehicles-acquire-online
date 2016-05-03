package controllers

import helpers.TestWithApplication
import helpers.UnitSpec
import models.VehicleTaxOrSornFormModel.Form.{SelectId, SornId, SornVehicleId}

class VehicleTaxOrSornFormSpec extends UnitSpec {

  final val SornSelected = "true"
  final val SornNotSelected = ""

  "form" should {
    "accept when sorn is selected" in new TestWithApplication {
      formWithValidDefaults().get.sornVehicle should equal(Some("true"))
    }

  }

  private def formWithValidDefaults(sornVehicle: String = SornSelected, selectedId: String = SornId) = {
    injector.getInstance(classOf[VehicleTaxOrSorn])
      .form.bind(
        Map(
          SornVehicleId -> sornVehicle,
          SelectId -> selectedId
        )
      )
  }
}