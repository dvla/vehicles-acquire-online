package controllers.acquire

import helpers.UnitSpec
import org.joda.time.LocalDate
import models.PrivateKeeperDetailsCompleteFormModel.Form.{DateOfBirthId, MileageId, ConsentId, DateOfSaleId}
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{DayId, MonthId, YearId}
import models.PrivateKeeperDetailsCompleteFormModel
import play.api.data.Form
import pages.acquire.PrivateKeeperDetailsCompletePage._
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
      model.dateOfSale should equal(new LocalDate(
        YearDateOfSaleValid.toInt,
        MonthDateOfSaleValid.toInt,
        DayDateOfSaleValid.toInt))
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(
        dayDateOfBirth = "",
        monthDateOfBirth = "",
        yearDateOfBirth = "",
        mileage = "").get
      model.dateOfBirth should equal(None)
      model.mileage should equal(None)
      model.dateOfSale should equal(new LocalDate(
        YearDateOfSaleValid.toInt,
        MonthDateOfSaleValid.toInt,
        DayDateOfSaleValid.toInt))
    }

    "reject if form has no fields completed" in {
      formWithValidDefaults(dayDateOfSale = "", monthDateOfSale = "", yearDateOfSale = "", consent = "").
        errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid", "error.required")
    }
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

  "date of sale" should {
    "not accept a date in the future" in {
      info("@@@@@@@@@@@@@@@@@@@@@@@@@@@ " + formWithValidDefaults(yearDateOfSale = "2500").errors.flatMap(_.messages) )

      formWithValidDefaults(yearDateOfSale = "2500").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.inTheFuture")
    }

    "not accept an invalid day of month of 0" in {
      formWithValidDefaults(dayDateOfSale = "0").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "not accept an invalid day of month of 32" in {
      info("%%%%%%%%%%%%%%%%%%%%%%%%% " + formWithValidDefaults(yearDateOfSale = "2500").errors.flatMap(_.messages) )
      formWithValidDefaults(dayDateOfSale = "32").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "not accept an invalid month of 0" in {
      formWithValidDefaults(monthDateOfSale = "0").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "not accept an invalid month of 13" in {
      formWithValidDefaults(monthDateOfSale = "13").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "not accept special characters in day field" in {
      formWithValidDefaults(dayDateOfSale = "$").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "not accept special characters in month field" in {
      formWithValidDefaults(monthDateOfSale = "$").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "not accept special characters in year field" in {
      formWithValidDefaults(yearDateOfSale = "$").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "not accept letters in day field" in {
      formWithValidDefaults(dayDateOfSale = "a").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "not accept letters in month field" in {
      formWithValidDefaults(monthDateOfSale = "a").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "not accept letters in year field" in {
      formWithValidDefaults(yearDateOfSale = "a").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid")
    }

    "accept if date of sale is entered correctly" in {
      val model = formWithValidDefaults(
        dayDateOfSale = DayDateOfSaleValid,
        monthDateOfSale = MonthDateOfSaleValid,
        yearDateOfSale = YearDateOfSaleValid).get

      model.dateOfSale should equal (new LocalDate(
        YearDateOfSaleValid.toInt,
        MonthDateOfSaleValid.toInt,
        DayDateOfSaleValid.toInt))
    }
  }


  private def formWithValidDefaults(dayDateOfBirth: String = DayDateOfBirthValid,
                                    monthDateOfBirth: String = MonthDateOfBirthValid,
                                    yearDateOfBirth: String = YearDateOfBirthValid,
                                    mileage: String = MileageValid,
                                    dayDateOfSale: String = DayDateOfSaleValid,
                                    monthDateOfSale: String = MonthDateOfSaleValid,
                                    yearDateOfSale: String = YearDateOfSaleValid,
                                    consent: String = ConsentTrue): Form[PrivateKeeperDetailsCompleteFormModel] = {
    injector.getInstance(classOf[PrivateKeeperDetailsComplete])
      .form.bind(
        Map(
          s"$DateOfBirthId.$DayId" -> dayDateOfBirth,
          s"$DateOfBirthId.$MonthId" -> monthDateOfBirth,
          s"$DateOfBirthId.$YearId" -> yearDateOfBirth,
          MileageId -> mileage,
          s"$DateOfSaleId.$DayId" -> dayDateOfSale,
          s"$DateOfSaleId.$MonthId" -> monthDateOfSale,
          s"$DateOfSaleId.$YearId" -> yearDateOfSale,
          ConsentId -> consent
        )
      )
  }
}