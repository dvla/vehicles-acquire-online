package controllers

import composition.WithApplication
import helpers.UnitSpec
import models.VehicleTaxOrSornFormModel.Form.{SornVehicleId, SelectId}

class VehicleTaxOrSornFormSpec extends UnitSpec {

  final val SornSelected = "true"
  final val SornNotSelected = ""

  "form" should {
    "accept when sorn is selected" in new WithApplication {
      formWithValidDefaults().get.sornVehicle should equal(Some("true"))
    }

//    "accept when sorn is not selected" in new WithApplication {
//      formWithValidDefaults(sornVehicle = SornNotSelected, selectedId = "T").get.sornVehicle should equal(None)
//    }
  }

  private def formWithValidDefaults(sornVehicle: String = SornSelected, selectedId: String = "S") = {
    injector.getInstance(classOf[VehicleTaxOrSorn])
      .form.bind(
        Map(
          SornVehicleId -> sornVehicle,
          SelectId -> selectedId
        )
      )
  }
}