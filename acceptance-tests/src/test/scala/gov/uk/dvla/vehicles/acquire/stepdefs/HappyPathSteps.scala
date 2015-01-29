package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire.AcquireFailurePage
import pages.acquire.AcquireSuccessPage
import pages.acquire.BeforeYouStartPage
import pages.acquire.BusinessChooseYourAddressPage
import pages.acquire.BusinessKeeperDetailsPage
import pages.acquire.CompleteAndConfirmPage
import pages.acquire.EnterAddressManuallyPage
import pages.acquire.NewKeeperChooseYourAddressPage
import pages.acquire.NewKeeperEnterAddressManuallyPage
import pages.acquire.PrivateKeeperDetailsPage
import pages.acquire.SetupTradeDetailsPage
import pages.acquire.VehicleLookupPage
import pages.acquire.VehicleTaxOrSornPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDriver, WebBrowserDSL}

/**
 *
 * Acquire screen flow
 * ==================
 * before-you-start
 * setup-trade-details
 *               |                  \
 * business-choose-your-address enter-address-manually
 *                       vehicle-lookup
 *         /                                          \
 *       private-keeper-details               business-keeper-details
 *           |                    \
 * new-keeper-choose-your-address new-keeper-enter-adress-manually
 * vehicle-tax-or-sorn
 *        complete-and-confirm
 *           /            \
 * acquire-success    acquire-failure
 *
 */
class HappyPathSteps(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  // Results in a transaction failure when used for both business name and private keeper first name
  private final val NameWhichResultsInTransactionFailure = "testcase1"
  private final val Postcode = "qq99qq"
  private final val FirstName = "joe"
  private final val LastName = "bloggs"
  private final val ValidVrn = "A1"
  private final val ValidDocRefNum = "1" * 11

  def goToSetupTradeDetailsPage() = {
    go to BeforeYouStartPage
    page.title should equal(BeforeYouStartPage.title)
    click on BeforeYouStartPage.startNow
    page.title should equal(SetupTradeDetailsPage.title)
  }

  def goToBusinessChooseYourAddressPage() = {
    goToSetupTradeDetailsPage()
    SetupTradeDetailsPage.traderName enter "VA12SU"
    SetupTradeDetailsPage.traderPostcode enter Postcode
    SetupTradeDetailsPage.traderEmail enter "C@GMAIL.COM"
    click on SetupTradeDetailsPage.lookup
    page.title should equal(BusinessChooseYourAddressPage.title)
  }

  def goToEnterAddressManuallyPage() = {
    goToBusinessChooseYourAddressPage()
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

  def fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage() = {
    goToVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber enter ValidVrn
    VehicleLookupPage.documentReferenceNumber enter ValidDocRefNum
  }

  def goToPrivateKeeperDetailsPage() = {
    fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage()
    click on VehicleLookupPage.vehicleSoldToPrivateIndividual
    click on VehicleLookupPage.next
    page.title should equal(PrivateKeeperDetailsPage.title)
  }

  def goToBusinessKeeperDetailsPage() = {
    fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage()
    click on VehicleLookupPage.vehicleSoldToBusiness
    click on VehicleLookupPage.next
    page.title should equal(BusinessKeeperDetailsPage.title)
  }

  def goToSelectNewKeeperAddressPage() = {
    goToPrivateKeeperDetailsPage()
    click on PrivateKeeperDetailsPage.mr
    PrivateKeeperDetailsPage.firstNameTextBox enter FirstName
    PrivateKeeperDetailsPage.lastNameTextBox enter LastName
    PrivateKeeperDetailsPage.postcodeTextBox enter Postcode
    click on PrivateKeeperDetailsPage.next
    page.title should equal(NewKeeperChooseYourAddressPage.title)
  }

  def goToVehicleTaxOrSornPage() = {
    goToSelectNewKeeperAddressPage()
    click on NewKeeperChooseYourAddressPage.manualAddress
    page.title should equal(NewKeeperEnterAddressManuallyPage.title)
    EnterAddressManuallyPage.addressBuildingNameOrNumber enter "1 highrate"
    EnterAddressManuallyPage.addressPostTown enter "swansea"
    click on EnterAddressManuallyPage.next
    page.title should equal(VehicleTaxOrSornPage.title)
  }

  def goToVehicleTaxOrSornPageWithKeeperAddressFromLookup(callNavigationSteps: Boolean = true) = {
    if (callNavigationSteps) goToSelectNewKeeperAddressPage()
    NewKeeperChooseYourAddressPage.chooseAddress.value = "0"
    click on NewKeeperChooseYourAddressPage.select
    page.title should equal(VehicleTaxOrSornPage.title)
  }

  def goToCompleteAndConfirmPage(callNavigationSteps: Boolean = true) = {
    if (callNavigationSteps) goToVehicleTaxOrSornPage()
    click on VehicleTaxOrSornPage.sornVehicle
    click on VehicleTaxOrSornPage.next
    page.title should equal(CompleteAndConfirmPage.title)
  }

  def goToCompleteAndConfirmPageAndNavigateBackwards() = {
    goToCompleteAndConfirmPage()
    click on CompleteAndConfirmPage.back
  }

  def goToAcquireSuccessPage() = {
    goToCompleteAndConfirmPage()
    CompleteAndConfirmPage.dayDateOfSaleTextBox enter "07"
    CompleteAndConfirmPage.monthDateOfSaleTextBox enter "08"
    CompleteAndConfirmPage.yearDateOfSaleTextBox enter "1999"
    click on CompleteAndConfirmPage.consent
    click on CompleteAndConfirmPage.next
    page.title should equal(AcquireSuccessPage.title)
  }


////
  /*
  def fillInBusinessKeeperDetailsPage() = {
    fillInVehicleDetailsPage()
    click on VehicleLookupPage.vehicleSoldToBusiness
    click on VehicleLookupPage.next
    page.title should equal(BusinessKeeperDetailsPage.title)
    BusinessKeeperDetailsPage.fleetNumberField enter "112233"
    BusinessKeeperDetailsPage.businessNameField enter "sole"
    BusinessKeeperDetailsPage.emailField enter "a@gmail.com"
    BusinessKeeperDetailsPage.postcodeField enter NoAddressesPostcode
    click on BusinessKeeperDetailsPage.next
  }

  def fillInPrivateKeeperDetailsPage() = {
    fillInVehicleDetailsPage()
    click on VehicleLookupPage.vehicleSoldToPrivateIndividual
    click on VehicleLookupPage.next
    page.title should equal(PrivateKeeperDetailsPage.title)
    click on PrivateKeeperDetailsPage.mr
    PrivateKeeperDetailsPage.firstNameTextBox enter FirstName
    PrivateKeeperDetailsPage.lastNameTextBox enter LastName
    PrivateKeeperDetailsPage.postcodeTextBox enter NoAddressesPostcode
    click on PrivateKeeperDetailsPage.next
  }

  def fillInNewKeeperChooseYourAddress() = {
    click on NewKeeperChooseYourAddressPage.manualAddress
    page.title should equal(NewKeeperEnterAddressManuallyPage.title)
    EnterAddressManuallyPage.addressBuildingNameOrNumber enter "1 highrate"
    EnterAddressManuallyPage.addressPostTown enter "swansea"
    click on EnterAddressManuallyPage.next
  }

  def navigateAfterCustomerTypeSelection(navigateToNextPage: Boolean) = {
    fillInNewKeeperChooseYourAddress()
    click on VehicleTaxOrSornPage.sornVehicle
    click on VehicleTaxOrSornPage.next
    CompleteAndConfirmPage.dayDateOfSaleTextBox enter "07"
    CompleteAndConfirmPage.monthDateOfSaleTextBox enter "08"
    CompleteAndConfirmPage.yearDateOfSaleTextBox enter "1999"
    click on CompleteAndConfirmPage.consent
    if(navigateToNextPage)
      click on CompleteAndConfirmPage.next
    else{
      click on CompleteAndConfirmPage.back
    }
  }

  def fillInPrivateKeeperDetailsPageWithFailureScreen() = {
    fillInVehicleDetailsPage()
    click on VehicleLookupPage.vehicleSoldToPrivateIndividual
    click on VehicleLookupPage.next
    click on PrivateKeeperDetailsPage.mr
    PrivateKeeperDetailsPage.firstNameTextBox enter NameWhichResultsInTransactionFailure
    PrivateKeeperDetailsPage.lastNameTextBox enter LastName
    PrivateKeeperDetailsPage.postcodeTextBox enter NoAddressesPostcode
    click on PrivateKeeperDetailsPage.next
  }
*/

  def goToCompleteAndConfirmPageWithTransactionFailureData() {
    goToPrivateKeeperDetailsPage()
    click on PrivateKeeperDetailsPage.mr
    PrivateKeeperDetailsPage.firstNameTextBox enter NameWhichResultsInTransactionFailure
    PrivateKeeperDetailsPage.lastNameTextBox enter LastName
    PrivateKeeperDetailsPage.postcodeTextBox enter Postcode
    click on PrivateKeeperDetailsPage.next
    page.title should equal(NewKeeperChooseYourAddressPage.title)

    goToVehicleTaxOrSornPageWithKeeperAddressFromLookup(callNavigationSteps = false)
    goToCompleteAndConfirmPage(callNavigationSteps = false)
    CompleteAndConfirmPage.dayDateOfSaleTextBox enter "07"
    CompleteAndConfirmPage.monthDateOfSaleTextBox enter "08"
    CompleteAndConfirmPage.yearDateOfSaleTextBox enter "1999"
    click on CompleteAndConfirmPage.consent
  }

  @Given("^the user is on the Business choose your address page$")
  def the_user_is_on_the_Business_choose_your_address_page() {
    goToBusinessChooseYourAddressPage()
  }

  @Given("^the user is on the Private keeper details page$")
  def the_user_is_on_the_Private_keeper_details_page() {
    goToPrivateKeeperDetailsPage()
  }

  @Given("^the user is on the Provide trader details page$")
  def the_user_is_on_the_Provide_trader_details_page() {
    goToSetupTradeDetailsPage()
  }

  @Given("^the user is on the New keeper choose your address page having selected a new private keeper$")
  def the_user_is_on_the_New_keeper_choose_your_address_page_having_selected_a_new_private_keeper() {
    goToSelectNewKeeperAddressPage()
  }

  @Given("^the user is on the Vehicle tax or sorn page with keeper address entered manually$")
  def the_user_is_on_the_Vehicle_tax_or_sorn_page() {
    goToVehicleTaxOrSornPage()
  }

  @Given("^the user is on the Vehicle tax or sorn page with keeper address from lookup$")
  def the_user_is_on_the_Vehicle_tax_or_sorn_page_with_keeper_address_from_lookup() {
    goToVehicleTaxOrSornPageWithKeeperAddressFromLookup()
  }

  @When("^the user navigates forwards from business choose your address page and there are no validation errors$")
  def the_user_navigates_forwards_from_business_choose_your_address_page_and_there_are_no_validation_errors() = {
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    click on BusinessChooseYourAddressPage.next
  }

  @When("^the user navigates forwards from business choose your address page to the enter address manually page$")
  def the_user_navigates_forwards_from_business_choose_your_address_page_to_the_enter_address_manually_page() = {
    click on BusinessChooseYourAddressPage.manualAddress
  }

  @When("^the user navigates backwards from the business choose your address page$")
  def the_user_navigates_backwards_from_the_business_choose_your_address_page() = {
    click on BusinessChooseYourAddressPage.back
  }

  @When("^the user navigates forwards from new keeper choose your address page$")
  def the_user_navigates_forwards_from_new_keeper_choose_your_address_page() = {
    NewKeeperChooseYourAddressPage.chooseAddress.value = "0"
    click on NewKeeperChooseYourAddressPage.select
  }

  @When("^the user navigates forwards from new keeper choose your address to the new keeper enter address manually page$")
  def the_user_navigates_forwards_from_new_keeper_choose_your_address_to_the_new_keeper_enter_address_manually_page() = {
    click on NewKeeperChooseYourAddressPage.manualAddress
  }

  @When("^the user navigates backwards from the new keeper choose your address$")
  def the_user_navigates_backwards_from_the_new_keeper_choose_your_address() = {
    click on NewKeeperChooseYourAddressPage.back
  }

  @When("^the user navigates forwards from the vehicle tax or sorn page and there are no validation errors$")
  def the_user_navigates_forwards_from_the_vehicle_tax_or_sorn_page_and_there_are_no_validation_errors() = {
    VehicleTaxOrSornPage.navigate()
  }

  @When("^the user navigates backwards from the vehicle tax or sorn page$")
  def the_user_navigates_backwards_from_the_vehicle_tax_or_sorn_page() = {
    click on VehicleTaxOrSornPage.back
  }

  @When("^the user navigates forwards from the complete and confirm page and there are no validation errors$")
  def the_user_navigates_forwards_from_the_complete_and_confirm_page_and_there_are_no_validation_errors() = {
    CompleteAndConfirmPage.navigate()
  }


//  @When("^the trader entered through successful postcode lookup$")
//  def the_trader_entered_through_successful_postcode_lookup() {
//    SetupTradeDetailsPage.happyPath()
//  }

//  @When("^entered valid registration number and doc reference number$")
//  def entered_valid_registration_number_and_doc_reference_number() {
//    fillInBusinessKeeperDetailsPage()
//    navigateAfterCustomerTypeSelection(navigateToNextPage = true)
//  }


  @When("^the user chooses private keeper and performs the vehicle lookup$")
  def the_user_chooses_private_keeper_and_performs_the_vehicle_lookup(): Unit = {
    goToPrivateKeeperDetailsPage()
  }

  @When("^the user on Private Keeper details page and entered through successful postcode lookup$")
  def the_user_on_Private_Keeper_details_page_and_entered_through_successful_postcode_lookup() {
    PrivateKeeperDetailsPage.navigate()
  }

  @When("^the user on Business Keeper details page and entered through successful postcode lookup$")
  def the_user_on_Business_Keeper_details_page_and_entered_through_successful_postcode_lookup() {
    BusinessKeeperDetailsPage.navigate()
  }

  @When("^the trader entered through unsuccessful postcode lookup$")
  def the_trader_entered_through_unsuccessful_postcode_lookup() {
    goToVehicleLookupPage()
  }

/*
  @When("^the user on Business Keeper details page and entered through unsuccessful postcode lookup$")
  def the_user_on_Business_Keeper_details_page_and_entered_through_unsuccessful_postcode_lookup() {
    fillInBusinessKeeperDetailsPage()
    navigateAfterCustomerTypeSelection(navigateToNextPage = true)
  }

  @When("^the user on Private Keeper details page and entered through unsuccessful postcode lookup$")
  def the_user_on_Private_Keeper_details_page_and_entered_through_unsuccessful_postcode_lookup() {
    fillInPrivateKeeperDetailsPage()
    navigateAfterCustomerTypeSelection(navigateToNextPage = true)
  }

  @When("^the user on Private Keeper details page and entered through unsuccessful postcode lookup with failure data$")
  def the_user_on_Private_Keeper_details_page_and_entered_through_unsuccessful_postcode_lookup_with_failure_data() {
    fillInPrivateKeeperDetailsPageWithFailureScreen()
    navigateAfterCustomerTypeSelection(navigateToNextPage = true)
  }

  @Then("^the user will be on confirmed summary page$")
  def the_user_will_be_on_confirmed_summary_page() {
    fillInPrivateKeeperDetailsPage()
    navigateAfterCustomerTypeSelection(navigateToNextPage = true)
    page.title should equal(AcquireSuccessPage.title)
  }
*/

  @Then("^the user is taken to the Private Keeper details page$")
  def the_user_is_taken_to_the_private_keeper_details_page() {
    page.title should equal(PrivateKeeperDetailsPage.title)
  }

  @Then("^the user will be on confirmed transaction failure screen$") // todo remove confirmed
  def the_user_will_be_on_confirmed_transaction_failure_screen() {
    page.title should equal(AcquireFailurePage.title)
  }
}
