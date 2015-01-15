package controllers.acquire

import controllers.VehicleTaxOrSorn
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import pages.acquire.{CompleteAndConfirmPage, VehicleLookupPage}
import play.api.test.{FakeRequest, WithApplication}
import pages.acquire.BusinessKeeperDetailsPage.{BusinessNameValid, FleetNumberValid, EmailValid}
import pages.acquire.PrivateKeeperDetailsPage.{FirstNameValid, LastNameValid}
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import webserviceclients.fakes.FakeVehicleAndKeeperLookupWebService.{RegistrationNumberValid, VehicleMakeValid, VehicleModelValid}

class VehicleTaxOrSornUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "present a pre-poulated form when the vehicle sorn cookie indicates the user has sorned the vehicle" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel(sornVehicle = Some("true")))
      val content = contentAsString(vehicleTaxOrSorn.present(request))
      content should include("checked") // Sorn checkbox value
    }

    "present an empty form when the vehicle sorn cookie indicates the user has not sorned the vehicle" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
      val content = contentAsString(vehicleTaxOrSorn.present(request))
      content should not include "checked" // Sorn checkbox value
    }

    "redirect to vehicle lookup when no new keeper details cookie is in cache" in new WithApplication {
      val request = FakeRequest().withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      val result = vehicleTaxOrSorn.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to vehicle lookup when no vehicle details cookie is in cache" in new WithApplication {
      val request = FakeRequest().withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel())
      val result = vehicleTaxOrSorn.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "play back business keeper details as expected" in new WithApplication() {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
        businessName = Some(BusinessNameValid),
        fleetNumber = Some(FleetNumberValid),
        email = Some(EmailValid),
        isBusinessKeeper = true
      )).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
      val content = contentAsString(vehicleTaxOrSorn.present(request))
      content should include("<dt>Fleet number</dt>")
      content should include(s"$BusinessNameValid")
      content should include(s"$FleetNumberValid")
      content should include(s"$EmailValid")
    }

    "play back private keeper details as expected" in new WithApplication() {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel(
        firstName = Some(FirstNameValid),
        lastName = Some(LastNameValid),
        email = Some(EmailValid),
        isBusinessKeeper = false
      )).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
      val content = contentAsString(vehicleTaxOrSorn.present(request))
      content should include(s"$FirstNameValid")
      content should include(s"$LastNameValid")
      content should include(s"$EmailValid")
    }

    "play back vehicle details as expected" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
      val content = contentAsString(vehicleTaxOrSorn.present(request))
      content should include(s"<dd>$RegistrationNumberValid</dd>")
      content should include(s"<dd>$VehicleMakeValid</dd>")
      content should include(s"<dd>$VehicleModelValid</dd>")
    }
  }

  "submit" should {
    "redirect to next page without sorning the vehicle" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel())
      val result = vehicleTaxOrSorn.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(CompleteAndConfirmPage.address))
      }
    }

    "redirect to next page with sorning the vehicle" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.vehicleTaxOrSornFormModel(sornVehicle = Some("true")))
      val result = vehicleTaxOrSorn.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(CompleteAndConfirmPage.address))
      }
    }
  }

  private val vehicleTaxOrSorn = {
    injector.getInstance(classOf[VehicleTaxOrSorn])
  }

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.newKeeperDetailsModel()).
      withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
    vehicleTaxOrSorn.present(request)
  }
}