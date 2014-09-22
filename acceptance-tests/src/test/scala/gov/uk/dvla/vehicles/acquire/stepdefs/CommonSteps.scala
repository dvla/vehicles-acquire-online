package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDriver, WebBrowserDSL}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire._


// TODO - Store input as variables

final class CommonSteps(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

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

  def goToBusinessKeeperDetailsPage() = {
    goToVehicleLookupPage()
    VehicleLookupPage.vehicleRegistrationNumber enter "A1"
    VehicleLookupPage.documentReferenceNumber enter "11111111111"
    click on VehicleLookupPage.vehicleSoldToBusiness
    click on VehicleLookupPage.next
  }

  def fillInBusinessKeeperDetailsPage() = {
    goToBusinessKeeperDetailsPage()
    BusinessKeeperDetailsPage.fleetNumberField enter "112233"
    BusinessKeeperDetailsPage.businessNameField enter "sole"
    BusinessKeeperDetailsPage.emailField enter "a@gmail.com"
    //click on BusinessKeeperDetailsPage.next
  }

  @Given("^that the user has selected \"(.*?)\" on the Enter vehicle details page for the question \"(.*?)\"$")
  def that_the_user_has_selected_on_the_Enter_vehicle_details_page_for_the_question(arg1: String, arg2: String) {
    // Write code here that turns the phrase above into concrete actions
  }

  @When("^the user is on the \"(.*?)\" page$")
  def the_user_is_on_the_page(arg1: String) {
    // Write code here that turns the phrase above into concrete actions
  }

  @Then("^there is a label titled \"Fleet number$")
  def there_is_a_label_titled_Fleet_number() {
  }



  //  Given("""^that the user has selected "(.*?)" on the Enter vehicle details page for the question "(.*?)"$""") { (str1: String, str2: String) =>
//    goToBusinessKeeperDetailsPage()
//  }
//
//  When("""^the user is on the "(.*?)" page$""") { (str3: String) =>
//    fillInBusinessKeeperDetailsPage()
//  }
//
//  Then("""^there is a label titled "(.*?)"$""") { (str4: String) =>
//    goToBusinessKeeperDetailsPage()
//  }
}