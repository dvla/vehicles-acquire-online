package controllers.acquire

import helpers.UnitSpec
import controllers.PrivateKeeperDetails
import pages.acquire.PrivateKeeperDetailsPage.{TitleValid, EmailValid, FirstNameValid}
import viewmodels.PrivateKeeperDetailsViewModel.Form.{TitleId, EmailId, FirstNameId}

class PrivateKeeperDetailsFormSpec extends UnitSpec {

  "form" should {
    "accept if form is completed with all fields correctly" in {
      val model = formWithValidDefaults(
        title = TitleValid,
        firstName = FirstNameValid,
        email = EmailValid).get
      model.title should equal(TitleValid)
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(
        title = TitleValid,
        firstName = FirstNameValid,
        email = "").get
      model.title should equal(TitleValid)
    }

    "reject if form has no fields completed" in {
      formWithValidDefaults(title = "", firstName = "", email = "").errors should have length 4
    }
  }

  "title" should {
    "reject if no selection is made" in {
      formWithValidDefaults(title = "").errors should have length 1
    }

    "accept if title is selected" in {
      val model = formWithValidDefaults(
        title = TitleValid).get
      model.title should equal(TitleValid)
    }
  }

  "email" should {
    "accept in valid format" in {
      val model = formWithValidDefaults(
        email = EmailValid).get
      model.email should equal(Some(EmailValid))
    }

    "accept with no entry" in {
      val model = formWithValidDefaults(
        email = "").get
      model.email should equal(None)
    }

    "reject if incorrect format" in {
      formWithValidDefaults(email = "no_at_symbol.com").errors should have length 1
    }

    "reject if less than min length" in {
      formWithValidDefaults(email = "no").errors should have length 1
    }

    "reject if greater than max length" in {
      formWithValidDefaults(email = "n@" + ("a" * 248) + ".com").errors should have length 1
    }

    "firstName" should {
      "reject if empty" in {
        formWithValidDefaults(firstName = "").errors.flatMap(_.messages) should contain theSameElementsAs
          List("error.minLength", "error.validFirstName", "error.required")
      }
    }
  }

  private def formWithValidDefaults(title: String = TitleValid, firstName: String = FirstNameValid, email: String = EmailValid) = {
    injector.getInstance(classOf[PrivateKeeperDetails])
      .form.bind(
        Map(
          TitleId -> title,
          FirstNameId -> firstName,
          EmailId -> email
        )
      )
  }
}
