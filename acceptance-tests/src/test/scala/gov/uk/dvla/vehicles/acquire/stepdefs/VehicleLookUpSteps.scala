package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{And, Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDriver, WithClue}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

class VehicleLookUpSteps(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers with WithClue {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  lazy val happyPath = new HappyPathSteps(webBrowserDriver)

  private final val VehicleNotDisposedVrn = "AA11AAE"
  private final val VehicleNotDisposedDocReferenceNumber = "88888888885"
  // Will result in the legacy stubs throwing a GetVehicleAndKeeperDetailsVehicleNotFoundException
  // and the ms will return a response code of
  // VMPR1 - vehicle_and_keeper_lookup_vrm_not_found
  private final val VrnNotFound = "C1"
  private final val ValidDocReferenceNumber = "1" * 11
  // Legacy stubs will return doc ref of 11111111111 so we get a response code from the ms of
  // L0002 - vehicle_and_keeper_lookup_document_reference_mismatch
  private final val InvalidDocReferenceNumber = "1" * 10 + "2"

  def gotoVehicleLookUpPageWithKnownAddress() {
    happyPath.goToSetupTradeDetailsPage()
    SetupTradeDetailsPage.traderName.value = "VA12SU"
    SetupTradeDetailsPage.traderPostcode.value = "qq99qq"
    click on SetupTradeDetailsPage.emailVisible
    SetupTradeDetailsPage.traderEmail.value = "C@GMAIL.COM"
    SetupTradeDetailsPage.traderConfirmEmail.value = "C@GMAIL.COM"
    click on SetupTradeDetailsPage.lookup
    BusinessChooseYourAddressPage.chooseAddress.value = BusinessChooseYourAddressPage.defaultSelectedAddress
    click on BusinessChooseYourAddressPage.select
    click on BusinessChooseYourAddressPage.next
  }

  @Given("^the user is on the Vehicle lookup page with trade address from lookup$")
  def the_user_is_on_the_Vehicle_lookup_page_with_trade_address_from_lookup() {
    gotoVehicleLookUpPageWithKnownAddress()
  }

  @Given("^the user is on the Vehicle lookup page with trade address entered manually$")
  def the_user_is_on_the_Vehicle_lookup_page_with_trade_address_entered_manually() {
    happyPath.goToVehicleLookupPageAfterManuallyEnteringAddress()
  }

  @Given("^the user is on the Enter new keeper details page$")
  def the_user_is_on_the_Enter_business_keeper_details_page() {
    gotoVehicleLookUpPageWithKnownAddress()
    the_user_fills_in_the_vrn_doc_ref_number_and_selects_businessKeeper()
    click on VehicleLookupPage.next
    pageTitle should equal(BusinessKeeperDetailsPage.title) withClue trackingId
  }

  @When("^the user fills in the vrn, doc ref number and selects privateKeeper$")
  def the_user_fill_in_the_vrn_doc_ref_number_and_selects_privateKeeper() {
    happyPath.fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber.value = RandomVrmGenerator.uniqueVrm
    VehicleLookupPage.documentReferenceNumber.value = ValidDocReferenceNumber
    click on VehicleLookupPage.vehicleSoldToPrivateIndividual
  }

  @When("^the user fills in the vrn, doc ref number and selects businessKeeper$")
  def the_user_fills_in_the_vrn_doc_ref_number_and_selects_businessKeeper() {
    happyPath.fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber.value = RandomVrmGenerator.uniqueVrm
    VehicleLookupPage.documentReferenceNumber.value = ValidDocReferenceNumber
    click on VehicleLookupPage.vehicleSoldToBusiness
  }

  @When("^the user fills in data that results in document reference mismatch error from the micro service$")
  def the_user_fills_in_data_that_results_in_document_reference_mismatch_error_from_the_micro_service() {
    happyPath.fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber.value = RandomVrmGenerator.uniqueVrm
    VehicleLookupPage.documentReferenceNumber.value = InvalidDocReferenceNumber
    click on VehicleLookupPage.vehicleSoldToBusiness
  }

  @When("^the user fills in data for a vehicle which has not been disposed$")
  def the_user_fills_in_data_for_a_vehicle_which_has_not_been_disposed() {
    happyPath.goToVehicleLookupPageAfterManuallyEnteringAddress()
    VehicleLookupPage.vehicleRegistrationNumber.value = VehicleNotDisposedVrn
    VehicleLookupPage.documentReferenceNumber.value = VehicleNotDisposedDocReferenceNumber
    click on VehicleLookupPage.vehicleSoldToBusiness
  }

  @When("^the user navigates forwards from the vehicle lookup page and there are no validation errors$")
  def the_user_navigates_forwards_from_the_vehicle_lookup_page_and_there_are_no_validation_errors() {
    click on VehicleLookupPage.next
  }

  @When("^the user selects the control labelled VehicleLookUpBack button$") //TODO: when the user navigates to the previous page
  def the_user_selects_the_control_labelled_VehicleLookUpBack_button() {
    click on VehicleLookupPage.back
  }

  @When("^the user navigates forwards from the business keeper details page and there are no validation errors$")
  def the_user_navigates_forwards_from_the_business_keeper_details_page_and_there_are_no_validation_errors() {
    BusinessKeeperDetailsPage.navigate()
  }

  @When("^the user navigates backwards from the business keeper details page$")
  def the_user_navigates_backwards_from_the_business_keeper_details_page() {
    click on BusinessKeeperDetailsPage.back
  }

  @When("^the user navigates forwards from private keeper details page and there are no validation errors$")
  def the_user_navigates_forwards_from_private_keeper_details_page_and_there_are_no_validation_errors() = {
    PrivateKeeperDetailsPage.navigate()
  }

  @When("^the user navigates backwards from private keeper details page$")
  def the_user_navigates_backwards_from_private_keeper_details_page() = {
    click on PrivateKeeperDetailsPage.back
  }

  @And("^the user performs the lookup$")
  def the_user_performs_the_lookup() {
    click on VehicleLookupPage.next
  }

  @Then("^the user remains on the VehicleLookPage$")
  def the_user_remains_on_the_VehicleLookPage() {
    pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
  }

  @Then("^the user is taken to the page entitled \"(.*?)\"$")
  def the_user_is_taken_to_the_page_entitled(title: String) {
    pageTitle shouldEqual title withClue trackingId
  }

  @Then("^the user will be redirected to the vehicle lookup failure page$")
  def the_user_will_be_redirected_to_the_VehicleLookupFailure_page() {
    pageTitle shouldEqual VehicleLookupFailurePage.title withClue trackingId
  }

  @Then("^the page will contain text \"(.*?)\"$")
  def the_page_will_contain_text(text: String) {
    pageSource should include(text) withClue trackingId
  }
}
