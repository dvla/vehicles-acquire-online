package controllers.acquire

import helpers.UnitSpec
import controllers.SetUpTradeDetails
import viewmodels.SetupTradeDetailsViewModel.Form._
import pages.acquire.SetupTradeDetailsPage.{TraderBusinessNameValid,PostcodeValid, TraderEmailValid}
import uk.gov.dvla.vehicles.presentation.common.mappings.BusinessName

class SetupTradeDetailsFormSpec extends UnitSpec {

  "form" should {
    "accept if form is completed with all fields correctly" in {
      val model = formWithValidDefaults(
        traderBusinessName = TraderBusinessNameValid,
        traderPostcode = PostcodeValid,
        traderEmail = TraderEmailValid).get
      model.traderBusinessName should equal(TraderBusinessNameValid)
      model.traderPostcode should equal(PostcodeValid)
      model.traderEmail should equal(Some(TraderEmailValid))
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(traderBusinessName = TraderBusinessNameValid, traderPostcode = PostcodeValid).get
      model.traderBusinessName should equal(TraderBusinessNameValid)
      model.traderPostcode should equal(PostcodeValid)
    }

    "reject if form has no fields completed" in {
      formWithValidDefaults(traderBusinessName = "", traderPostcode = "").errors should have length 6
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
      errors(2).message should equal("error.validBusinessName")
    }

    "reject if trader business name is less than minimum length" in {
      formWithValidDefaults(traderBusinessName = "A").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.minLength")
    }

    "reject if trader business name is more than the maximum length" in {
      formWithValidDefaults(traderBusinessName = "A" * BusinessName.MaxLength + 1).errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.maxLength")
    }

    "accept if trader business name is valid" in {
      formWithValidDefaults(traderBusinessName = TraderBusinessNameValid, traderPostcode = PostcodeValid).
        get.traderBusinessName should equal(TraderBusinessNameValid)
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
      formWithValidDefaults(traderPostcode = "M15A").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.minLength", "error.restricted.validPostcode")
    }

    "reject if trader postcode is more than the maximum length" in {
      formWithValidDefaults(traderPostcode = "SA99 1DDD").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.maxLength", "error.restricted.validPostcode")
    }

    "reject if trader postcode contains special characters" in {
      formWithValidDefaults(traderPostcode = "SA99 1D$").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.restricted.validPostcode")
    }

    "reject if trader postcode contains an incorrect format" in {
      formWithValidDefaults(traderPostcode = "SAR99").errors.flatMap(_.messages) should contain theSameElementsAs
        List("error.restricted.validPostcode")
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
