package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDriver, WebBrowserDSL}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire._
import webserviceclients.fakes.FakeVehicleLookupWebService._


// TODO - Store input as variables

final class VAHappyPath(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  def goToSetupTradeDetailsPage() = {
    go to BeforeYouStartPage
    page.title should equal(BeforeYouStartPage.title)
    click on BeforeYouStartPage.startNow
    page.title should equal(SetupTradeDetailsPage.title)
  }

  def goToEnterAddressManuallyPage() = {
    goToSetupTradeDetailsPage()
    SetupTradeDetailsPage.traderName enter "VA12SU"
    SetupTradeDetailsPage.traderPostcode enter "AA99 1AA"
    SetupTradeDetailsPage.traderEmail enter "C@GMAIL.COM"
    click on SetupTradeDetailsPage.lookup
    page.title should equal(BusinessChooseYourAddressPage.title)
    click on BusinessChooseYourAddressPage.manualAddress
    page.title should equal(EnterAddressManuallyPage.title)
  }

  def goToVehicleLookupPage() = {
    goToEnterAddressManuallyPage()
    page.title should equal(EnterAddressManuallyPage.title)
    EnterAddressManuallyPage.addressBuildingNameOrNumber enter "1 Long Road"
    EnterAddressManuallyPage.addressPostTown enter "Swansea"
    click on EnterAddressManuallyPage.next
    page.title should equal(VehicleLookupPage.title)
  }

  def fillInVehicleDetailsPage() = {
    goToVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber enter "A1"
    VehicleLookupPage.documentReferenceNumber enter "11111111111"

  }

  def fillInBusinessKeeperDetailsPage() = {
    fillInVehicleDetailsPage()
    click on VehicleLookupPage.vehicleSoldToBusiness
    click on VehicleLookupPage.next
    BusinessKeeperDetailsPage.fleetNumberField enter "112233"
    BusinessKeeperDetailsPage.businessNameField enter "sole"
    BusinessKeeperDetailsPage.emailField enter "a@gmail.com"
    BusinessKeeperDetailsPage.postcodeField enter "qw78qw"
    click on BusinessKeeperDetailsPage.next
    navigateAfterCustmourTypeSelection()
  }
  def fillInPrivateKeeperDetailsPage() = {
    fillInVehicleDetailsPage()
    click on VehicleLookupPage.vehicleSoldToPrivateIndividual
    click on VehicleLookupPage.next
    click on PrivateKeeperDetailsPage.mr
    PrivateKeeperDetailsPage.firstNameTextBox enter "jhkhkhk"
    PrivateKeeperDetailsPage.lastNameTextBox enter "hgjjghj"
    PrivateKeeperDetailsPage.postcodeTextBox enter "dd34dd"
    click on PrivateKeeperDetailsPage.next
    navigateAfterCustmourTypeSelection()
  }

  def navigateAfterCustmourTypeSelection() = {
    click on NewKeeperChooseYourAddressPage.manualAddress
    EnterAddressManuallyPage.addressBuildingNameOrNumber enter "1 highrate"
    EnterAddressManuallyPage.addressPostTown enter "swansea"
    click on EnterAddressManuallyPage.next
    /*CompleteAndConfirmPage.dayDateOfSaleTextBox enter "07"
    CompleteAndConfirmPage.monthDateOfSaleTextBox enter "08"
    CompleteAndConfirmPage.yearDateOfSaleTextBox enter "1999"
    CompleteAndConfirmPage.next*/
  }

  @Given("^the user is on the Provide trader details page$")
  def the_user_is_on_the_Provide_trader_details_page()  {
    goToSetupTradeDetailsPage()
  }

  @When("^the trader entered through successful postcode lookup\\.$")
  def the_trader_entered_through_successful_postcode_lookup(){

   SetupTradeDetailsPage.happyPath()

  }

  @When("^entered valid registration number and doc reference number$")
  def entered_valid_registration_number_and_doc_reference_number(): Unit ={
    fillInBusinessKeeperDetailsPage()
  }

  @When("^the user on Private Keeper details page and entered through successful postcode lookup$")
  def the_user_on_Private_Keeper_details_page_and_entered_through_successful_postcode_lookup(){
    PrivateKeeperDetailsPage.navigate()
  }

  @When("^the user on Business Keeper details page and entered through successful postcode lookup$")
  def the_user_on_Business_Keeper_details_page_and_entered_through_successful_postcode_lookup(){
   BusinessKeeperDetailsPage.navigate()
  }

  @When("^the trader entered through unsuccessful postcode lookup\\.$")
  def the_trader_entered_through_unsuccessful_postcode_lookup(){
    goToVehicleLookupPage()
  }

  @When("^the user on Business Keeper details page and entered through unsuccessful postcode lookup$")
  def the_user_on_Business_Keeper_details_page_and_entered_through_unsuccessful_postcode_lookup(){
     fillInBusinessKeeperDetailsPage()
  }

  @When("^the user on Private Keeper details page and entered through unsuccessful postcode lookup$")
  def the_user_on_Private_Keeper_details_page_and_entered_through_unsuccessful_postcode_lookup() {
    fillInPrivateKeeperDetailsPage()
  }

  @Then("^the user will be on confirmed summary page\\.$")
  def the_user_will_be_on_confirmed_summary_page(){

  }
}
