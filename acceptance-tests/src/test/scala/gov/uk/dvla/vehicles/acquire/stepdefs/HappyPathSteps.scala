package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import org.joda.time.DateTime
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
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
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

/**
 *
 * Acquire screen flow
 *
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
class HappyPathSteps(webBrowserDriver: WebBrowserDriver) extends gov.uk.dvla.vehicles.acquire.helpers.AcceptanceTestHelper {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  // Results in a transaction failure when used for both business name and private keeper first name
  private final val NameWhichResultsInTransactionFailure = "testcase"
  private final val Postcode = "qq99qq"
  private final val FirstName = "joe"
  private final val LastName = "bloggs"
  private final val ValidDocRefNum = "1" * 11

  private final val today = DateTime.now
  private final val previousYear = today.minusYears(1).getYear

  def goToSetupTradeDetailsPage() = {
    go to BeforeYouStartPage
    pageTitle shouldEqual BeforeYouStartPage.title withClue trackingId
    click on BeforeYouStartPage.startNow
    pageTitle shouldEqual SetupTradeDetailsPage.title withClue trackingId
  }

  def goToBusinessChooseYourAddressPage() = {
    goToSetupTradeDetailsPage()
    SetupTradeDetailsPage.traderName.value = "VA12SU"
    SetupTradeDetailsPage.traderPostcode.value = Postcode
    click on SetupTradeDetailsPage.emailVisible
    SetupTradeDetailsPage.traderEmail.value = "C@GMAIL.COM"
    SetupTradeDetailsPage.traderConfirmEmail.value = "C@GMAIL.COM"
    click on SetupTradeDetailsPage.lookup
    pageTitle shouldEqual BusinessChooseYourAddressPage.title withClue trackingId
  }

  def goToEnterAddressManuallyPage() = {
    goToBusinessChooseYourAddressPage()
    click on BusinessChooseYourAddressPage.manualAddress
    pageTitle shouldEqual EnterAddressManuallyPage.title withClue trackingId
  }

  def goToVehicleLookupPageAfterManuallyEnteringAddress() = {
    goToEnterAddressManuallyPage()
    EnterAddressManuallyPage.addressBuildingNameOrNumber.value = "1 Long Road"
    EnterAddressManuallyPage.addressPostTown.value = "Swansea"
    EnterAddressManuallyPage.addressPostcode.value = "SA1 1AA"
    click on EnterAddressManuallyPage.next
    pageTitle shouldEqual VehicleLookupPage.title withClue trackingId
  }

  def fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage(
    vrn: String = RandomVrmGenerator.uniqueVrm,
    v5c: String = ValidDocRefNum) = {
    goToVehicleLookupPageAfterManuallyEnteringAddress()
    VehicleLookupPage.vehicleRegistrationNumber.value = vrn
    VehicleLookupPage.documentReferenceNumber.value = v5c
  }

  def goToPrivateKeeperDetailsPage(callNavigationSteps: Boolean = true) = {
    if (callNavigationSteps) fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage()
    click on VehicleLookupPage.vehicleSoldToPrivateIndividual
    click on VehicleLookupPage.next
    pageTitle shouldEqual PrivateKeeperDetailsPage.title withClue trackingId
  }

  def goToBusinessKeeperDetailsPage() = {
    fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage()
    click on VehicleLookupPage.vehicleSoldToBusiness
    click on VehicleLookupPage.next
    pageTitle shouldEqual BusinessKeeperDetailsPage.title withClue trackingId
  }

  def goToSelectNewKeeperAddressPageAfterFillingInNewPrivateKeeper(callNavigationSteps: Boolean = true) = {
    if (callNavigationSteps) goToPrivateKeeperDetailsPage()
    click on PrivateKeeperDetailsPage.mr
    PrivateKeeperDetailsPage.firstNameTextBox.value = FirstName
    PrivateKeeperDetailsPage.lastNameTextBox.value = LastName
    PrivateKeeperDetailsPage.postcodeTextBox.value = Postcode
    click on PrivateKeeperDetailsPage.emailInvisible
    click on PrivateKeeperDetailsPage.next
    pageTitle shouldEqual NewKeeperChooseYourAddressPage.title withClue trackingId
  }

  def goToSelectNewKeeperAddressPageAfterFillingInNewBusinessKeeper() = {
    goToBusinessKeeperDetailsPage()
    click on BusinessKeeperDetailsPage.fleetNumberVisible
    BusinessKeeperDetailsPage.fleetNumberField.value = "112233"
    BusinessKeeperDetailsPage.businessNameField.value = "test business"
    click on BusinessKeeperDetailsPage.emailVisible
    BusinessKeeperDetailsPage.emailField.value = "a@gmail.com"
    BusinessKeeperDetailsPage.emailConfirmField.value = "a@gmail.com"
    BusinessKeeperDetailsPage.postcodeField.value = Postcode
    click on BusinessKeeperDetailsPage.next
    pageTitle shouldEqual NewKeeperChooseYourAddressPage.title withClue trackingId
  }

  def goToNewKeeperEnterAddressManuallyAfterFillingInNewBusinessKeeper() = {
    goToSelectNewKeeperAddressPageAfterFillingInNewBusinessKeeper()
    click on NewKeeperChooseYourAddressPage.manualAddress
    pageTitle shouldEqual NewKeeperEnterAddressManuallyPage.title withClue trackingId
  }

  def goToVehicleTaxOrSornPage(callNavigationSteps: Boolean = true) = {
    if (callNavigationSteps) goToSelectNewKeeperAddressPageAfterFillingInNewPrivateKeeper()
    click on NewKeeperChooseYourAddressPage.manualAddress
    pageTitle shouldEqual NewKeeperEnterAddressManuallyPage.title withClue trackingId
    EnterAddressManuallyPage.addressBuildingNameOrNumber.value = "1 highrate"
    EnterAddressManuallyPage.addressPostTown.value = "swansea"
    EnterAddressManuallyPage.addressPostcode.value = "sa11aa"
    click on EnterAddressManuallyPage.next
    pageTitle shouldEqual VehicleTaxOrSornPage.title withClue trackingId
  }

  def goToVehicleTaxOrSornPageWithKeeperAddressFromLookup(callNavigationSteps: Boolean = true) = {
    if (callNavigationSteps) goToSelectNewKeeperAddressPageAfterFillingInNewPrivateKeeper()
    NewKeeperChooseYourAddressPage.chooseAddress.value = BusinessChooseYourAddressPage.defaultSelectedAddress
    click on NewKeeperChooseYourAddressPage.select
    pageTitle shouldEqual VehicleTaxOrSornPage.title withClue trackingId
  }

  def goToCompleteAndConfirmPage(callNavigationSteps: Boolean = true) = {
    if (callNavigationSteps) goToVehicleTaxOrSornPage()
    click on VehicleTaxOrSornPage.sornSelect
    click on VehicleTaxOrSornPage.next
    pageTitle shouldEqual CompleteAndConfirmPage.title withClue trackingId
  }

  def goToCompleteAndConfirmPage(vrn: String, v5c: String) = {
    fillInVehicleDetailsButNotTheKeeperOnVehicleLookupPage(vrn, v5c)
    goToPrivateKeeperDetailsPage(callNavigationSteps = false)

    goToSelectNewKeeperAddressPageAfterFillingInNewPrivateKeeper(callNavigationSteps = false)

    goToVehicleTaxOrSornPage(callNavigationSteps = false)
    click on VehicleTaxOrSornPage.sornSelect
    click on VehicleTaxOrSornPage.next
    pageTitle shouldEqual CompleteAndConfirmPage.title withClue trackingId
  }

  def goToCompleteAndConfirmPageAndNavigateBackwards() = {
    goToCompleteAndConfirmPage()
    click on CompleteAndConfirmPage.back
    pageTitle shouldEqual VehicleTaxOrSornPage.title withClue trackingId
  }

  def fillInCompleteAndConfirm(day: String = "01", month: String = "01", year: String = previousYear.toString) = {
    CompleteAndConfirmPage.dayDateOfSaleTextBox.value = day
    CompleteAndConfirmPage.monthDateOfSaleTextBox.value = month
    CompleteAndConfirmPage.yearDateOfSaleTextBox.value = year
    click on CompleteAndConfirmPage.consent
  }

  def goToAcquireSuccessPage() = {
    goToCompleteAndConfirmPage()
    fillInCompleteAndConfirm(
      day = today.toString("dd"),
      month = today.toString("MM"),
      year = today.getYear.toString
    )
    click on CompleteAndConfirmPage.next
    pageTitle shouldEqual AcquireSuccessPage.title withClue trackingId
  }

  def goToCompleteAndConfirmPageWithTransactionFailureData() {
    goToPrivateKeeperDetailsPage()
    click on PrivateKeeperDetailsPage.mr
    PrivateKeeperDetailsPage.firstNameTextBox.value = NameWhichResultsInTransactionFailure
    PrivateKeeperDetailsPage.lastNameTextBox.value = LastName
    PrivateKeeperDetailsPage.postcodeTextBox.value = Postcode
    click on PrivateKeeperDetailsPage.emailInvisible
    click on PrivateKeeperDetailsPage.next
    pageTitle should equal(NewKeeperChooseYourAddressPage.title)

    goToVehicleTaxOrSornPageWithKeeperAddressFromLookup(callNavigationSteps = false)
    goToCompleteAndConfirmPage(callNavigationSteps = false)
    fillInCompleteAndConfirm(
      day = today.toString("dd"),
      month = today.toString("MM"),
      year = today.getYear.toString
    )
  }

  private def fillInPrivateKeeperDetails(day: String = "01", month: String = "01", year: String = previousYear.toString) = {
    click on PrivateKeeperDetailsPage.mr
    PrivateKeeperDetailsPage.firstNameTextBox.value = FirstName
    PrivateKeeperDetailsPage.lastNameTextBox.value = LastName
    PrivateKeeperDetailsPage.postcodeTextBox.value = Postcode
    PrivateKeeperDetailsPage.dayDateOfBirthTextBox.value = day
    PrivateKeeperDetailsPage.monthDateOfBirthTextBox.value = month
    PrivateKeeperDetailsPage.yearDateOfBirthTextBox.value = year
    click on PrivateKeeperDetailsPage.emailInvisible
    click on PrivateKeeperDetailsPage.next
  }

  @Given("^the user is on the Enter address manually page$")
  def the_user_is_on_the_Enter_address_manually_page() {
    goToEnterAddressManuallyPage()
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
    goToSelectNewKeeperAddressPageAfterFillingInNewPrivateKeeper()
  }

  @Given("^the user is on the New keeper choose your address page having selected a new business keeper$")
  def the_user_is_on_the_New_keeper_choose_your_address_page_having_selected_a_new_business_keeper() {
    goToSelectNewKeeperAddressPageAfterFillingInNewBusinessKeeper()
  }

  @Given("^the user is on the New keeper enter address manually page$")
  def the_user_is_on_the_New_keeper_enter_address_manually_page() {
    goToNewKeeperEnterAddressManuallyAfterFillingInNewBusinessKeeper()
  }

  @Given("^the user is on the Vehicle tax or sorn page with keeper address entered manually$")
  def the_user_is_on_the_Vehicle_tax_or_sorn_page() {
    goToVehicleTaxOrSornPage()
  }

  @Given("^the user is on the Vehicle tax or sorn page with keeper address from lookup$")
  def the_user_is_on_the_Vehicle_tax_or_sorn_page_with_keeper_address_from_lookup() {
    goToVehicleTaxOrSornPageWithKeeperAddressFromLookup()
  }

  @When("^the user navigates forwards from enter address manually and there are no validation errors$")
  def the_user_navigates_forwards_from_enter_address_manually_and_there_are_no_validation_errors() = {
    EnterAddressManuallyPage.addressBuildingNameOrNumber.value = "1 Long Road"
    EnterAddressManuallyPage.addressPostTown.value = "Swansea"
    EnterAddressManuallyPage.addressPostcode.value = "SA1 1AA"
    click on EnterAddressManuallyPage.next
  }

  @When("^the user navigates backwards from the enter address manually page$")
  def the_user_navigates_backwards_from_the_enter_address_manually_page() = {
    click on EnterAddressManuallyPage.back
  }

  @When("^the user navigates forwards from business choose your address page and there are no validation errors$")
  def the_user_navigates_forwards_from_business_choose_your_address_page_and_there_are_no_validation_errors() = {
    BusinessChooseYourAddressPage.chooseAddress.value = BusinessChooseYourAddressPage.defaultSelectedAddress
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
    NewKeeperChooseYourAddressPage.chooseAddress.value = BusinessChooseYourAddressPage.defaultSelectedAddress
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

  @When("^the user navigates forwards from new keeper enter address manually and there are no validation errors$")
  def the_user_navigates_forwards_from_new_keeper_enter_address_manually_and_there_are_no_validation_errors() = {
    NewKeeperEnterAddressManuallyPage.addressBuildingNameOrNumber.value = "1 Long Road"
    NewKeeperEnterAddressManuallyPage.addressPostTown.value = "Swansea"
    NewKeeperEnterAddressManuallyPage.addressPostcode.value = "SA1 1AA"
    click on NewKeeperEnterAddressManuallyPage.next
  }

  @When("^the user navigates backwards from the new keeper enter address manually page$")
  def the_user_navigates_backwards_from_the_new_keeper_enter_address_manually_page() = {
    click on NewKeeperEnterAddressManuallyPage.back
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

  @When("^the user chooses private keeper and performs the vehicle lookup$")
  def the_user_chooses_private_keeper_and_performs_the_vehicle_lookup() = {
    goToPrivateKeeperDetailsPage()
  }

  @When("^the user on Private Keeper details page and entered through successful postcode lookup$")
  def the_user_on_Private_Keeper_details_page_and_entered_through_successful_postcode_lookup() {
    PrivateKeeperDetailsPage.navigate()
  }

  @When("^the user enters an invalid date of birth and submits the form$")
  def the_user_enters_an_invalid_date_of_birth_and_submits_the_form() = {
    fillInPrivateKeeperDetails(year = "201")
  }

  @When("^the user enters a date of birth more than 110 years in the past and submits the form$")
  def the_user_enters_a_date_of_birth_more_than_110_years_in_the_past_and_submits_the_form() = {
    val veryOldYear = today.minusYears(111)
    fillInPrivateKeeperDetails(year = veryOldYear.getYear.toString)
  }

  @When("^the user enters a date of birth in the future and submits the form$")
  def the_user_enters_a_date_of_birth_in_the_future_and_submits_the_form() = {
    val futureDay = today.plusDays(1)
    fillInPrivateKeeperDetails(day = f"${futureDay.getDayOfMonth}%02d", month = f"${futureDay.getMonthOfYear}%02d", year = f"${futureDay.getYear}%02d")
  }

  @When("^the user on Business Keeper details page and entered through successful postcode lookup$")
  def the_user_on_Business_Keeper_details_page_and_entered_through_successful_postcode_lookup() {
    BusinessKeeperDetailsPage.navigate()
  }

  @When("^the trader entered through unsuccessful postcode lookup$")
  def the_trader_entered_through_unsuccessful_postcode_lookup() {
    goToVehicleLookupPageAfterManuallyEnteringAddress()
  }

  @Then("^the user is taken to the Private Keeper details page$")
  def the_user_is_taken_to_the_private_keeper_details_page() {
    pageTitle shouldEqual PrivateKeeperDetailsPage.title withClue trackingId
  }

  @Then("^there will be an error message displayed \"(.*?)\"$")
  def there_will_be_an_error_message_displayed(errMsg: String) {
    pageSource should include(errMsg) withClue trackingId
  }
}
