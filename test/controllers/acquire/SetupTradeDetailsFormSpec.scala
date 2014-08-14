package controllers.acquire

import helpers.UnitSpec
import controllers.SetUpTradeDetails
import viewmodels.SetupTradeDetailsViewModel.Form._
import pages.acquire.SetupTradeDetailsPage.{TraderBusinessNameValid,PostcodeValid}

class SetupTradeDetailsFormSpec extends UnitSpec {

  "form" should {
    "accept if form is valid with all fields filled in" in {
      val model = formWithValidDefaults(traderBusinessName = TraderBusinessNameValid, traderPostcode = PostcodeValid).get
      model.traderBusinessName should equal(TraderBusinessNameValid.toUpperCase)
      model.traderPostcode should equal(PostcodeValid)
    }
  }

  "TraderBusinessName" should {
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

  private def formWithValidDefaults(traderBusinessName: String = TraderBusinessNameValid,
                                    traderPostcode: String = PostcodeValid) = {

    injector.getInstance(classOf[SetUpTradeDetails])
      .form.bind(
        Map(
          TraderNameId -> traderBusinessName,
          TraderPostcodeId -> traderPostcode
        )
      )
  }
}
