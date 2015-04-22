package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire.{BusinessChooseYourAddressPage, SetupTradeDetailsPage, VehicleLookupPage, BusinessKeeperDetailsPage}
import uk.gov.dvla.vehicles.presentation.common.helpers
import helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver,WithClue}

class SuppressionFlag(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers with WithClue {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  lazy val happyPath = new HappyPathSteps(webBrowserDriver)

  def goToVehicleLookUpPage()={
    happyPath.goToSetupTradeDetailsPage()
    SetupTradeDetailsPage.traderName enter "VA12SU"
    SetupTradeDetailsPage.traderPostcode enter "qq99qq"
    click on SetupTradeDetailsPage.emailVisible
    SetupTradeDetailsPage.traderEmail enter "C@GMAIL.COM"
    click on SetupTradeDetailsPage.lookup
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    click on BusinessChooseYourAddressPage.next
  }

  @Given("^the user is in vehicle lookup page$")
  def the_user_is_in_vehicle_lookup_page()  {
    goToVehicleLookUpPage()
    page.title shouldEqual VehicleLookupPage.title withClue trackingId
  }

  @When("^the user  enter the vehicle details which has a supression flag and click on submit$")
  def the_user_enter_the_vehicle_details_which_has_a_supression_flag_and_click_on_submit()  {
    VehicleLookupPage.vehicleRegistrationNumber enter "AA11AAF"
    VehicleLookupPage.documentReferenceNumber enter "88888888881"
    click on VehicleLookupPage.vehicleSoldToBusiness
    click on VehicleLookupPage.next
    page.source should include ("Buy another vehicle") withClue trackingId
    page.source should include ("Finish") withClue trackingId
  }

  @When("^the user enters the vehicle details which does not have a supression flag and click on Next button without any validation errors$")
  def the_user_enters_the_vehicle_details_which_does_not_have_a_supression_flag_and_click_on_Next_button_without_any_validation_errors()  {
    VehicleLookupPage.vehicleRegistrationNumber enter "A1"
    VehicleLookupPage.documentReferenceNumber enter "11111111111"
    click on VehicleLookupPage.vehicleSoldToBusiness
    click on VehicleLookupPage.next
  }

  @Then("^the user will successfully navigate to the next page$")
  def the_user_will_successfully_navigate_to_the_next_page()  {
     page.title shouldEqual BusinessKeeperDetailsPage.title withClue trackingId
  }
}
