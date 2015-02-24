package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Given, When, Then}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire.{BusinessKeeperDetailsPage, BusinessChooseYourAddressPage, SetupTradeDetailsPage, VehicleLookupPage}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}

class CheckKeeperEndDateBeforeProccessingAcquireTransaction(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  lazy val happyPath = new HappyPathSteps(webBrowserDriver)

  def gotoVehicleLookUpPageWithKnownAddress() {
    happyPath.goToSetupTradeDetailsPage()
    SetupTradeDetailsPage.traderName enter "VA12SU"
    SetupTradeDetailsPage.traderPostcode enter "qq99qq"
    SetupTradeDetailsPage.traderEmail enter "C@GMAIL.COM"
    click on SetupTradeDetailsPage.lookup
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    click on BusinessChooseYourAddressPage.next
  }

  @Given("^the user is on the Vehicle LookUp page$")
  def the_user_is_on_the_Vehicle_LookUp_page()  {
    gotoVehicleLookUpPageWithKnownAddress()
    page.title shouldEqual VehicleLookupPage.title
  }

  @When("^the user has submitted a vehicle lookup request and a matching record is returned$")
  def the_user_has_submitted_a_vehicle_lookup_request_and_a_matching_record_is_returned()  {
      VehicleLookupPage.vehicleRegistrationNumber enter "AA11AAE"
      VehicleLookupPage.documentReferenceNumber enter "88888888885"
      click on VehicleLookupPage.vehicleSoldToBusiness
      click on VehicleLookupPage.next
  }

  @Then("^the user is presented with an error message \"(.*?)\"$")
  def the_user_is_presented_with_an_error_message(keeperStillOnRecord:String)  {
    page.source should include(keeperStillOnRecord)
  }

  @When("^the user has submitted a vehicle lookup request which does n't have keeper end date$")
  def the_user_has_submitted_a_vehicle_lookup_request_which_does_n_t_have_keeper_end_date()  {
    VehicleLookupPage.vehicleRegistrationNumber enter "AA11AAA"
    VehicleLookupPage.documentReferenceNumber enter "88888888881"
    click on VehicleLookupPage.vehicleSoldToBusiness
    click on VehicleLookupPage.next
  }

  @Then("^the user will navigate to next page successfully$")
  def the_user_will_navigate_to_next_page_successfully()  {
    page.title shouldEqual BusinessKeeperDetailsPage.title
  }

}
