package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire.BusinessChooseYourAddressPage
import pages.acquire.BusinessKeeperDetailsPage
import pages.acquire.SetupTradeDetailsPage
import pages.acquire.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDriver, WebBrowserDSL}

class VehicleLookUpSteps(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  lazy val vaHappyPath = new VAHappyPathSteps(webBrowserDriver)

  def gotoVehicleLookUpPageWithKnownAddress() {
    vaHappyPath.goToSetupTradeDetailsPage()
    SetupTradeDetailsPage.traderName enter "VA12SU"
    SetupTradeDetailsPage.traderPostcode enter "qq99qq"
    SetupTradeDetailsPage.traderEmail enter "C@GMAIL.COM"
    click on SetupTradeDetailsPage.lookup
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    click on BusinessChooseYourAddressPage.next
  }

  @Given("^the user is on the Vehicle lookup page$")
  def the_user_is_on_the_Vehicle_lookup_page() {
    gotoVehicleLookUpPageWithKnownAddress()
  }

  @Then("^the user will be stay on the VehicleLookPage$")
  def the_user_will_be_stay_on_the_VehicleLookPage() {
    page.title should equal(VehicleLookupPage.title)
  }

  @When("^the user fill the vrn doc ref number and select privateKeeper$")
  def the_user_fill_the_vrn_doc_ref_number_and_select_privateKeeper() {
    vaHappyPath.fillInVehicleDetailsPage()
    VehicleLookupPage.vehicleRegistrationNumber enter "A1"
    VehicleLookupPage.documentReferenceNumber enter "11111111111"
    vaHappyPath.click on VehicleLookupPage.vehicleSoldToPrivateIndividual
  }

  @When("^the user fill the vrn doc ref number and select businessKeeper$")
  def the_user_fill_the_vrn_doc_ref_number_and_select_businessKeeper() {
    vaHappyPath.fillInVehicleDetailsPage()
    VehicleLookupPage.vehicleRegistrationNumber enter "A1"
    VehicleLookupPage.documentReferenceNumber enter "11111111111"
    vaHappyPath.click on VehicleLookupPage.vehicleSoldToBusiness
  }

  @When("^the user selects the control labelled Next$")
  def the_user_selects_the_control_labelled_Next()  {
    click on VehicleLookupPage.next
  }

  @Then("^the user will be taken to the page titled \"(.*?)\" page$")
  def the_user_will_be_taken_to_the_page_titled_page(title:String) {
    title match {
      case "Enter new keeper details"      => page.title should equal("Enter new keeper details")
      case "Enter business keeper details" => page.title should equal("Enter business keeper details")
      case "Select new keeper address"     => page.title should equal("Select new keeper address")
      case "Vehicle lookup"                => page.title should equal("Enter vehicle details")
    }
  }

  @When("^the user selects the  control labelled VehicleLookUpBack button$")
  def the_user_selects_the_control_labelled_VehicleLookUpBack_button() {
    click on VehicleLookupPage.back
  }

  @Then("^the user will be taken to the page titled Select trader address page$")
  def the_user_will_be_taken_to_the_page_titled_Select_trader_address_page(){
    page.title should equal(BusinessChooseYourAddressPage.title)
  }

  @Given("^the user is on the Enter business keeper details page$")
  def the_user_is_on_the_Enter_business_keeper_details_page()  {
    gotoVehicleLookUpPageWithKnownAddress()
    the_user_fill_the_vrn_doc_ref_number_and_select_businessKeeper()
    click on  VehicleLookupPage.next
    page.title should equal(BusinessKeeperDetailsPage.title)
  }

  @When("^the user selects the primary control labelled Next and there are no validation errors$")
  def the_user_selects_the_primary_control_labelled_Next_and_there_are_no_validation_errors()  {
    BusinessKeeperDetailsPage.navigate()
  }

  @When("^the user selects the secondary control labelled BusinessKeeperBack button$")
  def the_user_selects_the_secondary_control_labelled_BusinessKeeperBack_button() {
    click on BusinessKeeperDetailsPage.back
  }
}
