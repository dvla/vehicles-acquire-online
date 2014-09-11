package controllers.acquire

import helpers.UnitSpec
import controllers.BusinessKeeperDetails
import pages.acquire.BusinessKeeperDetailsPage.{FleetNumberValid, BusinessNameValid, EmailValid}
import uk.gov.dvla.vehicles.presentation.common.mappings.BusinessName
import viewmodels.BusinessKeeperDetailsFormViewModel.Form.{FleetNumberId, BusinessNameId, EmailId}

class BusinessKeeperDetailsFormSpec extends UnitSpec {

  "form" should {
    "accept if form is completed with all fields correct" in {
      val model = formWithValidDefaults().get
      model.fleetNumber should equal(Some(FleetNumberValid))
      model.businessName should equal(BusinessNameValid)
      model.email should equal(Some(EmailValid))
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(
        fleetNumber = "",
        email = "").get
      model.fleetNumber should equal(None)
      model.businessName should equal(BusinessNameValid)
      model.email should equal(None)
    }

    "reject if form has no fields completed" in {
      formWithValidDefaults(fleetNumber = "", businessName = "", email = "").errors should have length 3
    }
  }

  "businessName" should {
    "reject if business name is blank" in {
      // IMPORTANT: The messages being returned by the form validation are overridden by the Controller
      val errors = formWithValidDefaults(businessName = "").errors
      errors should have length 3
      errors(0).key should equal(BusinessNameId)
      errors(0).message should equal("error.minLength")
      errors(1).key should equal(BusinessNameId)
      errors(1).message should equal("error.required")
      errors(2).key should equal(BusinessNameId)
      errors(2).message should equal("error.validBusinessName")
    }

    "reject if business name is less than minimum length" in {
      formWithValidDefaults(businessName = "A").errors should have length 1
    }

    "reject if business name is more than the maximum length" in {
      formWithValidDefaults(businessName = "A" * BusinessName.MaxLength + 1).errors should have length 1
    }
  }

  private def formWithValidDefaults(fleetNumber: String = FleetNumberValid,
                                    businessName: String = BusinessNameValid,
                                    email: String = EmailValid) = {
    injector.getInstance(classOf[BusinessKeeperDetails])
      .form.bind(
        Map(
          FleetNumberId -> fleetNumber,
          BusinessNameId -> businessName,
          EmailId -> email
        )
      )
  }
}