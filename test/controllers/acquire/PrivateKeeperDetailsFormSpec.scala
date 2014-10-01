package controllers.acquire

import helpers.UnitSpec
import controllers.PrivateKeeperDetails
import org.joda.time.LocalDate
import pages.acquire.PrivateKeeperDetailsPage.TitleValid
import pages.acquire.PrivateKeeperDetailsPage.EmailValid
import pages.acquire.PrivateKeeperDetailsPage.FirstNameValid
import pages.acquire.PrivateKeeperDetailsPage.LastNameValid
import pages.acquire.PrivateKeeperDetailsPage.DriverNumberValid
import pages.acquire.PrivateKeeperDetailsPage.DayDateOfBirthValid
import pages.acquire.PrivateKeeperDetailsPage.MonthDateOfBirthValid
import pages.acquire.PrivateKeeperDetailsPage.YearDateOfBirthValid
import models.PrivateKeeperDetailsFormModel.Form.TitleId
import models.PrivateKeeperDetailsFormModel.Form.EmailId
import models.PrivateKeeperDetailsFormModel.Form.FirstNameId
import models.PrivateKeeperDetailsFormModel.Form.FirstNameMaxLength
import models.PrivateKeeperDetailsFormModel.Form.FirstNameMinLength
import models.PrivateKeeperDetailsFormModel.Form.DriverNumberId
import models.PrivateKeeperDetailsFormModel.Form.{LastNameId, LastNameMaxLength, LastNameMinLength, DateOfBirthId}
import uk.gov.dvla.vehicles.presentation.common.mappings.DayMonthYear.{YearId, MonthId, DayId}

class PrivateKeeperDetailsFormSpec extends UnitSpec {

  "form" should {
    "accept if form is completed with all fields correctly" in {
      val model = formWithValidDefaults(
        title = TitleValid,
        firstName = FirstNameValid,
        lastName = LastNameValid,
        email = EmailValid).get
      model.title should equal(TitleValid)
      model.firstName should equal(FirstNameValid)
      model.lastName should equal(LastNameValid)
      model.dateOfBirth should equal(Some(new LocalDate(
        YearDateOfBirthValid.toInt,
        MonthDateOfBirthValid.toInt,
        DayDateOfBirthValid.toInt)))
      model.email should equal(Some(EmailValid))
      model.driverNumber should equal(Some(DriverNumberValid))
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(
        title = TitleValid,
        firstName = FirstNameValid,
        lastName = LastNameValid,
        dayDateOfBirth = "",
        monthDateOfBirth = "",
        yearDateOfBirth = "",
        email = "",
        driverNumber = "").get
      model.title should equal(TitleValid)
      model.firstName should equal(FirstNameValid)
      model.lastName should equal(LastNameValid)
      model.dateOfBirth should equal(None)
      model.email should equal(None)
      model.driverNumber should equal(None)
    }

    "reject if form has no fields completed" in {
      formWithValidDefaults(title = "", firstName = "", lastName = "", email = "").
        errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.required", "error.minLength", "error.required", "error.validFirstName", "error.minLength", "error.required", "error.validLastName")
    }
  }

  "title" should {
    "reject if no selection is made" in {
      formWithValidDefaults(title = "").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.required")
    }

    "accept if title is selected" in {
      val model = formWithValidDefaults(title = TitleValid).get
      model.title should equal(TitleValid)
    }
  }

  "email" should {
    "accept in valid format" in {
      val model = formWithValidDefaults(email = EmailValid).get
      model.email should equal(Some(EmailValid))
    }

    "accept with no entry" in {
      val model = formWithValidDefaults(email = "").get
      model.email should equal(None)
    }

    "reject if incorrect format" in {
      formWithValidDefaults(email = "no_at_symbol.com").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.email")
    }

    "reject if less than min length" in {
      formWithValidDefaults(email = "no").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.email")
    }

    "reject if greater than max length" in {
      formWithValidDefaults(email = "n@" + ("a" * 248) + ".com").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.email")
    }
  }

  "firstName" should {
    "reject if empty" in {
      formWithValidDefaults(firstName = "").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.minLength", "error.validFirstName", "error.required")
    }

    "reject if greater than max length" in {
      formWithValidDefaults(firstName = "a" * (FirstNameMaxLength + 1)).errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.maxLength")
    }

    "reject if denied special characters are present $" in {
      formWithValidDefaults(firstName = FirstNameValid + "$").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.validFirstName")
    }

    "reject if denied special characters are present +" in {
      formWithValidDefaults(firstName = FirstNameValid + "+").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.validFirstName")
    }

    "reject if denied special characters are present ^" in {
      formWithValidDefaults(firstName = FirstNameValid + "^").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.validFirstName")
    }

    "reject if denied special characters are present *" in {
      formWithValidDefaults(firstName = FirstNameValid + "*").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.validFirstName")
    }

    "accept if equal to max length" in {
      val model = formWithValidDefaults(firstName = "a" * FirstNameMaxLength).get
      model.firstName should equal("a" * FirstNameMaxLength)
    }

    "accept if equal to min length" in {
      val model = formWithValidDefaults(firstName = "a" * FirstNameMinLength).get
      model.firstName should equal("a" * FirstNameMinLength)
    }

    "accept in valid format" in {
      val model = formWithValidDefaults(firstName = FirstNameValid).get
      model.firstName should equal(FirstNameValid)
    }

    "accept allowed special characters ." in {
      val model = formWithValidDefaults(firstName = FirstNameValid + ".").get
      model.firstName should equal(FirstNameValid + ".")
    }

    "accept allowed special characters ," in {
      val model = formWithValidDefaults(firstName = FirstNameValid + ",").get
      model.firstName should equal( FirstNameValid + ",")
    }

    "accept allowed special characters -" in {
      val model = formWithValidDefaults(firstName = FirstNameValid + "'").get
      model.firstName should equal(FirstNameValid + "'")
    }

    "accept allowed special characters \"" in {
      val model = formWithValidDefaults(firstName = FirstNameValid + "'").get
      model.firstName should equal(FirstNameValid + "'")
    }

    "accept allowed special characters '" in {
      val model = formWithValidDefaults(firstName = FirstNameValid + "'").get
      model.firstName should equal(FirstNameValid + "'")
    }

    "accept when a space is present within the first name" in {
      val model = formWithValidDefaults(firstName = "a" + " " + "a").get
      model.firstName should equal("a" + " " + "a")
    }
  }

  "lastName" should {
    "reject if empty" in {
      formWithValidDefaults(lastName = "").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.minLength", "error.validLastName", "error.required")
    }

    "reject if greater than max length" in {
      formWithValidDefaults(lastName = "a" * (LastNameMaxLength + 1)).errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.maxLength")
    }

    "reject if denied special characters are present $" in {
      formWithValidDefaults(lastName = LastNameValid + "$").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.validLastName")
    }

    "reject if denied special characters are present +" in {
      formWithValidDefaults(lastName = LastNameValid + "+").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.validLastName")
    }

    "reject if denied special characters are present ^" in {
      formWithValidDefaults(lastName = LastNameValid + "^").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.validLastName")
    }

    "reject if denied special characters are present *" in {
      formWithValidDefaults(lastName = LastNameValid + "*").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.validLastName")
    }

    "accept if equal to max length" in {
      val model = formWithValidDefaults(lastName = "a" * LastNameMaxLength).get
      model.lastName should equal("a" * LastNameMaxLength)
    }

    "accept if equal to min length" in {
      val model = formWithValidDefaults(lastName = "a" * LastNameMinLength).get
      model.lastName should equal("a" * LastNameMinLength)
    }

    "accept in valid format" in {
      val model = formWithValidDefaults(lastName = LastNameValid).get
      model.lastName should equal(LastNameValid)
    }

    "accept allowed special characters ." in {
      val model = formWithValidDefaults(lastName = LastNameValid + ".").get
      model.lastName should equal(LastNameValid + ".")
    }

    "accept allowed special characters ," in {
      val model = formWithValidDefaults(lastName = LastNameValid + ",").get
      model.lastName should equal( LastNameValid + ",")
    }

    "accept allowed special characters -" in {
      val model = formWithValidDefaults(lastName = LastNameValid + "'").get
      model.lastName should equal(LastNameValid + "'")
    }

    "accept allowed special characters \"" in {
      val model = formWithValidDefaults(lastName = LastNameValid + "'").get
      model.lastName should equal(LastNameValid + "'")
    }

    "accept allowed special characters '" in {
      val model = formWithValidDefaults(lastName = LastNameValid + "'").get
      model.lastName should equal(LastNameValid + "'")
    }

    "accept when a space is present within the first name" in {
      val model = formWithValidDefaults(lastName = "a" + " " + "a").get
      model.lastName should equal("a" + " " + "a")
    }
  }

  "date of birth" should {
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

  private def formWithValidDefaults(title: String = TitleValid,
                                    firstName: String = FirstNameValid,
                                    lastName: String = LastNameValid,
                                    dayDateOfBirth: String = DayDateOfBirthValid,
                                    monthDateOfBirth: String = MonthDateOfBirthValid,
                                    yearDateOfBirth: String = YearDateOfBirthValid,
                                    email: String = EmailValid,
                                    driverNumber: String = DriverNumberValid) = {
    injector.getInstance(classOf[PrivateKeeperDetails])
      .form.bind(
        Map(
          TitleId -> title,
          FirstNameId -> firstName,
          LastNameId -> lastName,
          s"$DateOfBirthId.$DayId" -> dayDateOfBirth,
          s"$DateOfBirthId.$MonthId" -> monthDateOfBirth,
          s"$DateOfBirthId.$YearId" -> yearDateOfBirth,
          EmailId -> email,
          DriverNumberId -> driverNumber
        )
      )
  }
}
