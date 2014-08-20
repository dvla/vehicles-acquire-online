package controllers.acquire

import helpers.UnitSpec
import controllers.SetUpTradeDetails
import viewmodels.SetupTradeDetailsViewModel.Form._
import pages.acquire.SetupTradeDetailsPage.{TraderBusinessNameValid,PostcodeValid, TraderEmailValid}

class SetupTradeDetailsFormSpec extends UnitSpec {

  "form" should {
    "accept if form is completed with all fields correctly" in {
      val model = formWithValidDefaults(traderBusinessName = TraderBusinessNameValid, traderPostcode = PostcodeValid, traderEmail = TraderEmailValid).get
      model.traderBusinessName should equal(TraderBusinessNameValid.toUpperCase)
      model.traderPostcode should equal(PostcodeValid)
      model.traderEmail should equal(Some(TraderEmailValid))
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(traderBusinessName = TraderBusinessNameValid, traderPostcode = PostcodeValid).get
      model.traderBusinessName should equal(TraderBusinessNameValid.toUpperCase)
      model.traderPostcode should equal(PostcodeValid)
    }

    "reject if form has no fields completed" in {
      formWithValidDefaults(traderBusinessName = "" , traderPostcode = "").errors should have length 6
    }
  }

  "traderBusinessName" should {
    "reject if trader business name is blank" in {
      // IMPORTANT: The messages being returned by the form validation are overridden by the Controller
      val errors = formWithValidDefaults(traderBusinessName = "").errors
      errors should have length 3
      errors(0).key should equal(TraderNameId)
      errors(0).message should equal("error.minLength")
      errors(1).key should equal(TraderNameId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(TraderNameId)
      errors(2).message should equal("error.validTraderBusinessName")
    }

    "reject if trader business name is less than minimum length" in {
      formWithValidDefaults(traderBusinessName = "A").errors should have length 1
    }

    "reject if trader business name is more than the maximum length" in {
      formWithValidDefaults(traderBusinessName = "A" * 101).errors should have length 1
    }

    "accept if trader business name is valid" in {
      formWithValidDefaults(traderBusinessName = TraderBusinessNameValid, traderPostcode = PostcodeValid).
        get.traderBusinessName should equal(TraderBusinessNameValid.toUpperCase)
    }
  }

  "postcode" should {
    "reject if trader postcode is empty" in {
      // IMPORTANT: The messages being returned by the form validation are overridden by the Controller
      val errors = formWithValidDefaults(traderPostcode = "").errors
      errors should have length 3
      errors(0).key should equal(TraderPostcodeId)
      errors(0).message should equal("error.minLength")
      errors(1).key should equal(TraderPostcodeId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(TraderPostcodeId)
      errors(2).message should equal("error.restricted.validPostcode")
    }

    "reject if trader postcode is less than the minimum length" in {
      formWithValidDefaults(traderPostcode = "M15A").errors should have length 2
    }

    "reject if trader postcode is more than the maximum length" in {
      formWithValidDefaults(traderPostcode = "SA99 1DDD").errors should have length 2
    }

    "reject if trader postcode contains special characters" in {
      formWithValidDefaults(traderPostcode = "SA99 1D$").errors should have length 1
    }

    "reject if trader postcode contains an incorrect format" in {
      formWithValidDefaults(traderPostcode = "SAR99").errors should have length 1
    }
  }

  "trader email" should {
    "reject if incorrect format is used" in {
      formWithValidDefaults(traderEmail = "EmailWithNoAtSymbol").errors should have length 1
    }

    "reject if greater than max length" in {
      val longInvalidEmail = "a@" + ("a" * 550) + ".com"
      formWithValidDefaults(traderEmail = longInvalidEmail).errors should have length 1
    }
  }

  private def formWithValidDefaults(traderBusinessName: String = TraderBusinessNameValid,
                                    traderPostcode: String = PostcodeValid, traderEmail: String = "") = {

    injector.getInstance(classOf[SetUpTradeDetails])
      .form.bind(
        Map(
          TraderNameId -> traderBusinessName,
          TraderPostcodeId -> traderPostcode,
          TraderEmailId -> traderEmail
        )
      )
  }
}
