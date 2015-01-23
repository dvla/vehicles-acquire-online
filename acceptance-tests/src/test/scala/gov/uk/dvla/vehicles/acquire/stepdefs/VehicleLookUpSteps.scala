package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{And, Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire.BusinessChooseYourAddressPage
import pages.acquire.BusinessKeeperDetailsPage
import pages.acquire.SetupTradeDetailsPage
import pages.acquire.VehicleLookupPage
import pages.acquire.VehicleLookupFailurePage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDriver, WebBrowserDSL}

class VehicleLookUpSteps(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  lazy val vaHappyPath = new VAHappyPathSteps(webBrowserDriver)

  private final val ValidVrn = "A1"
  // Will result in the legacy stubs throwing a GetVehicleAndKeeperDetailsVehicleNotFoundException
  // and the ms will return a response code of
  // VMPR1 - vehicle_and_keeper_lookup_vrm_not_found
  private final val VrnNotFound = "C1"
  private final val ValidDocReferenceNumber = "1" * 11
  // Legacy stubs will return doc ref of 11111111111 so we get a response code from the ms of
  // L0002 - vehicle_and_keeper_lookup_document_reference_mismatch
  private final val InvalidDocReferenceNumber = "1" * 10 + "2"

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

  @Given("^the user is on the Enter business keeper details page$")
  def the_user_is_on_the_Enter_business_keeper_details_page() {
    gotoVehicleLookUpPageWithKnownAddress()
    the_user_fills_in_the_vrn_doc_ref_number_and_selects_businessKeeper()
    click on VehicleLookupPage.next
    page.title should equal(BusinessKeeperDetailsPage.title)
  }

  @When("^the user fills in the vrn, doc ref number and selects privateKeeper$")
  def the_user_fill_in_the_vrn_doc_ref_number_and_selects_privateKeeper() {
    vaHappyPath.fillInVehicleDetailsPage()
    VehicleLookupPage.vehicleRegistrationNumber enter ValidVrn
    VehicleLookupPage.documentReferenceNumber enter ValidDocReferenceNumber
    vaHappyPath.click on VehicleLookupPage.vehicleSoldToPrivateIndividual
  }

  @When("^the user fills in the vrn, doc ref number and selects businessKeeper$")
  def the_user_fills_in_the_vrn_doc_ref_number_and_selects_businessKeeper() {
    vaHappyPath.fillInVehicleDetailsPage()
    VehicleLookupPage.vehicleRegistrationNumber enter ValidVrn
    VehicleLookupPage.documentReferenceNumber enter ValidDocReferenceNumber
    vaHappyPath.click on VehicleLookupPage.vehicleSoldToBusiness
  }

  @When("^the user fills in data that results in vrn not found error from the micro service$")
  def the_user_fills_in_data_that_results_in_a_vrn_not_found_error_from_the_micro_service() {
    vaHappyPath.fillInVehicleDetailsPage()
    VehicleLookupPage.vehicleRegistrationNumber enter VrnNotFound
    VehicleLookupPage.documentReferenceNumber enter ValidDocReferenceNumber
    vaHappyPath.click on VehicleLookupPage.vehicleSoldToBusiness
  }

  @When("^the user fills in data that results in document reference mismatch error from the micro service$")
  def the_user_fills_in_data_that_results_in_document_reference_mismatch_error_from_the_micro_service() = {
    vaHappyPath.fillInVehicleDetailsPage()
    VehicleLookupPage.vehicleRegistrationNumber enter ValidVrn
    VehicleLookupPage.documentReferenceNumber enter InvalidDocReferenceNumber
    vaHappyPath.click on VehicleLookupPage.vehicleSoldToBusiness
  }

  @When("^the user navigates to the next page$")
  def the_user_navigates_to_the_next_page() {
    click on VehicleLookupPage.next
  }

  @When("^the user selects the control labelled VehicleLookUpBack button$") //TODO: when the user navigates to the previous page
  def the_user_selects_the_control_labelled_VehicleLookUpBack_button() {
    click on VehicleLookupPage.back
  }

  @When("^the user selects the primary control labelled Next and there are no validation errors$")
  def the_user_selects_the_primary_control_labelled_Next_and_there_are_no_validation_errors() {
    BusinessKeeperDetailsPage.navigate()
  }

  @When("^the user selects the secondary control labelled BusinessKeeperBack button$")
  def the_user_selects_the_secondary_control_labelled_BusinessKeeperBack_button() {
    click on BusinessKeeperDetailsPage.back
  }

  @And("^the user performs the lookup$")
  def the_user_performs_the_lookup() {
    click on VehicleLookupPage.next
  }

  @Then("^the user remains on the VehicleLookPage$")
  def the_user_remains_on_the_VehicleLookPage() {
    page.title should equal(VehicleLookupPage.title)
  }

  @Then("^the user is taken to the page entitled \"(.*?)\"$")
  def the_user_is_taken_to_the_page_entitled(title: String) {
    page.title should equal(title)
  }

  @Then("^the user will be redirected to the vehicle lookup failure page$")
  def the_user_will_be_redirected_to_the_VehicleLookupFailure_page() {
    page.title should equal(VehicleLookupFailurePage.title)
  }

  @Then("^the page will contain text \"(.*?)\"$")
  def the_page_will_contain_text(text: String) {
    page.source should include(text)
  }
}
