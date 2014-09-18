package controllers.acquire

import helpers.UnitSpec
import models.BusinessKeeperDetailsCompleteFormModel.Form.MileageId
import models.BusinessKeeperDetailsCompleteFormModel
import play.api.data.Form
import pages.acquire.BusinessKeeperDetailsCompletePage.MileageValid
import scala.Some
import controllers.BusinessKeeperDetailsComplete

class BusinessKeeperDetailsCompleteFormSpec extends UnitSpec {

  "form" should {
    "accept if form is completed with all fields entered correctly" in {
      val model = formWithValidDefaults().get

      model.mileage should equal(Some("1000".toInt))
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(mileage = "").get

      model.mileage should equal(None)
    }

    // ToDo introduce test below when mandatory fields are implemented
    //    "reject if form has no fields completed" in {
    //      formWithValidDefaults(title = "", firstName = "", lastName = "", email = "").
    //        errors.flatMap(_.messages) should contain theSameElementsAs
    //        List("error.required", "error.minLength", "error.required", "error.validFirstName", "error.minLength", "error.required", "error.validLastName")
    //    }
  }

  "mileage" should {
    "not accept less than 0" in {
      formWithValidDefaults(mileage = "-1").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.min")
    }

    "not accept less than 999999" in {
      formWithValidDefaults(mileage = "1000000").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.max")
    }

    "not accept letters" in {
      formWithValidDefaults(mileage = "abc").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.number")
    }

    "not accept special characters %%" in {
      formWithValidDefaults(mileage = "%%").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.number")
    }

    "not accept special characters (" in {
      formWithValidDefaults(mileage = "(").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.number")
    }

    "accept if mileage is entered correctly" in {
      val model = formWithValidDefaults(mileage = MileageValid).get

      model.mileage should equal(Some(MileageValid.toInt))
    }
  }


  private def formWithValidDefaults(mileage: String = MileageValid): Form[BusinessKeeperDetailsCompleteFormModel] = {
    injector.getInstance(classOf[BusinessKeeperDetailsComplete])
      .form.bind(
        Map(
          MileageId -> mileage
        )
      )
  }
}