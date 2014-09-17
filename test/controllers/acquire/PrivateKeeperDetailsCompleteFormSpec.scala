package controllers.acquire

import helpers.UnitSpec
import org.joda.time.LocalDate
import models.PrivateKeeperDetailsCompleteFormModel.Form.{DateOfBirthId, MileageId}
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{DayId, MonthId, YearId}
import models.PrivateKeeperDetailsCompleteFormModel
import play.api.data.Form
import pages.acquire.PrivateKeeperDetailsCompletePage.{DayDateOfBirthValid, MonthDateOfBirthValid, YearDateOfBirthValid, MileageValid}
import scala.Some
import controllers.PrivateKeeperDetailsComplete

class PrivateKeeperDetailsCompleteFormSpec extends UnitSpec {

  "form" should {
    "accept if form is completed with all fields entered correctly" in {
      val model = formWithValidDefaults().get
      model.dateOfBirth should equal(Some(new LocalDate(
        YearDateOfBirthValid.toInt,
        MonthDateOfBirthValid.toInt,
        DayDateOfBirthValid.toInt)))
      model.mileage should equal(Some("1000".toInt))
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(
        dayDateOfBirth = "",
        monthDateOfBirth = "",
        yearDateOfBirth = "",
        mileage = "").get
      model.dateOfBirth should equal(None)
      model.mileage should equal(None)
    }

    // ToDo introduce test below when mandatory fields are implemented
    //    "reject if form has no fields completed" in {
    //      formWithValidDefaults(title = "", firstName = "", lastName = "", email = "").
    //        errors.flatMap(_.messages) should contain theSameElementsAs
    //        List("error.required", "error.minLength", "error.required", "error.validFirstName", "error.minLength", "error.required", "error.validLastName")
    //    }
  }


  "date of birth" should {
    "not accept a date in the future" in {
      formWithValidDefaults(yearDateOfBirth = "2500").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.inTheFuture")
    }

    "not accept an invalid day of month of 0" in {
      formWithValidDefaults(dayDateOfBirth = "0").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "not accept an invalid day of month of 32" in {
      formWithValidDefaults(dayDateOfBirth = "32").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "not accept an invalid month of 0" in {
      formWithValidDefaults(monthDateOfBirth = "0").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "not accept an invalid month of 13" in {
      formWithValidDefaults(monthDateOfBirth = "13").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "not accept special characters in day field" in {
      formWithValidDefaults(dayDateOfBirth = "$").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "not accept special characters in month field" in {
      formWithValidDefaults(monthDateOfBirth = "$").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "not accept special characters in year field" in {
      formWithValidDefaults(yearDateOfBirth = "$").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "not accept letters in day field" in {
      formWithValidDefaults(dayDateOfBirth = "a").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "not accept letters in month field" in {
      formWithValidDefaults(monthDateOfBirth = "a").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "not accept lettersin year field" in {
      formWithValidDefaults(yearDateOfBirth = "a").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.dateOfBirth.invalid")
    }

    "accept if date of birth is entered correctly" in {
      val model = formWithValidDefaults(
        dayDateOfBirth = DayDateOfBirthValid,
        monthDateOfBirth = MonthDateOfBirthValid,
        yearDateOfBirth = YearDateOfBirthValid).get

      model.dateOfBirth should equal(Some(new LocalDate(
        YearDateOfBirthValid.toInt,
        MonthDateOfBirthValid.toInt,
        DayDateOfBirthValid.toInt)))
    }
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


  private def formWithValidDefaults(dayDateOfBirth: String = DayDateOfBirthValid,
                                    monthDateOfBirth: String = MonthDateOfBirthValid,
                                    yearDateOfBirth: String = YearDateOfBirthValid,
                                    mileage: String = MileageValid): Form[PrivateKeeperDetailsCompleteFormModel] = {
    injector.getInstance(classOf[PrivateKeeperDetailsComplete])
      .form.bind(
        Map(
          s"$DateOfBirthId.$DayId" -> dayDateOfBirth,
          s"$DateOfBirthId.$MonthId" -> monthDateOfBirth,
          s"$DateOfBirthId.$YearId" -> yearDateOfBirth,
          MileageId -> mileage
        )
      )
  }
}