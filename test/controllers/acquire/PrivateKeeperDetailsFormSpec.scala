package controllers.acquire

import helpers.UnitSpec
import controllers.PrivateKeeperDetails
import pages.acquire.PrivateKeeperDetailsPage.{TitleValid, EmailValid, FirstNameValid, LastNameValid, DriverNumberValid}
import models.PrivateKeeperDetailsFormModel.Form.{TitleId, EmailId, FirstNameId, FirstNameMaxLength, FirstNameMinLength, DriverNumberId}
import models.PrivateKeeperDetailsFormModel.Form.{LastNameId, LastNameMaxLength, LastNameMinLength}

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
      model.email should equal(Some(EmailValid))
      model.driverNumber should equal(Some(DriverNumberValid))
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(
        title = TitleValid,
        firstName = FirstNameValid,
        lastName = LastNameValid,
        email = "",
        driverNumber = "").get
      model.title should equal(TitleValid)
      model.firstName should equal(FirstNameValid)
      model.lastName should equal(LastNameValid)
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

  private def formWithValidDefaults(title: String = TitleValid,
                                    firstName: String = FirstNameValid,
                                    lastName: String = LastNameValid,
                                    email: String = EmailValid,
                                    driverNumber: String = DriverNumberValid) = {
    injector.getInstance(classOf[PrivateKeeperDetails])
      .form.bind(
        Map(
          TitleId -> title,
          FirstNameId -> firstName,
          LastNameId -> lastName,
          EmailId -> email,
          DriverNumberId -> driverNumber
        )
      )
  }
}
