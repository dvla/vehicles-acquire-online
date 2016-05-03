package controllers

import helpers.TestWithApplication
import helpers.UnitSpec
import models.CompleteAndConfirmFormModel
import models.CompleteAndConfirmFormModel.Form.{ConsentId, DateOfSaleId, MileageId}
import org.joda.time.LocalDate
import pages.acquire.CompleteAndConfirmPage.{ConsentTrue, DayDateOfSaleValid, MileageValid, MonthDateOfSaleValid, YearDateOfSaleValid}
import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{DayId, MonthId, YearId}

class CompleteAndConfirmFormSpec extends UnitSpec {

  "form" should {
    "accept if form is completed with all fields entered correctly" in new TestWithApplication {
      val model = formWithValidDefaults().get
      model.mileage should equal(Some("1000".toInt))
      model.dateOfSale should equal(new LocalDate(
        YearDateOfSaleValid.toInt,
        MonthDateOfSaleValid.toInt,
        DayDateOfSaleValid.toInt))
      model.consent should equal ("Consent")
    }

    "accept if form is completed with mandatory fields only" in new TestWithApplication {
      val model = formWithValidDefaults(
        mileage = "").get
      model.mileage should equal(None)
      model.dateOfSale should equal(new LocalDate(
        YearDateOfSaleValid.toInt,
        MonthDateOfSaleValid.toInt,
        DayDateOfSaleValid.toInt))
    }

    "reject if form has no fields completed" in new TestWithApplication {
      formWithValidDefaults(dayDateOfSale = "", monthDateOfSale = "", yearDateOfSale = "", consent = "")
        .errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.date.invalid", "error.required")
    }
  }

  "mileage" should {
    "not accept less than 0" in new TestWithApplication {
      formWithValidDefaults(mileage = "-1").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.min")
    }

    "not accept less than 999999" in new TestWithApplication {
      formWithValidDefaults(mileage = "1000000").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.max")
    }

    "not accept letters" in new TestWithApplication {
      formWithValidDefaults(mileage = "abc").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.number")
    }

    "not accept special characters %%" in new TestWithApplication {
      formWithValidDefaults(mileage = "%%").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.number")
    }

    "not accept special characters (" in new TestWithApplication {
      formWithValidDefaults(mileage = "(").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.number")
    }

    "accept if mileage is entered correctly" in new TestWithApplication {
      val model = formWithValidDefaults(mileage = MileageValid).get

      model.mileage should equal(Some(MileageValid.toInt))
    }
  }

  private def formWithValidDefaults(mileage: String = MileageValid,
                                    dayDateOfSale: String = DayDateOfSaleValid,
                                    monthDateOfSale: String = MonthDateOfSaleValid,
                                    yearDateOfSale: String = YearDateOfSaleValid,
                                    consent: String = ConsentTrue): Form[CompleteAndConfirmFormModel] = {
    injector.getInstance(classOf[CompleteAndConfirm])
      .form.bind(
        Map(
          MileageId -> mileage,
          s"$DateOfSaleId.$DayId" -> dayDateOfSale,
          s"$DateOfSaleId.$MonthId" -> monthDateOfSale,
          s"$DateOfSaleId.$YearId" -> yearDateOfSale,
          ConsentId -> consent
        )
      )
  }
}
