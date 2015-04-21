package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire.{VehicleLookupPage, BusinessChooseYourAddressPage, SetupTradeDetailsPage}
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDSL, WebBrowserDriver}
import uk.gov.dvla.vehicles.presentation.common.testhelpers.RandomVrmGenerator

class SystemAuditVehicleSteps(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

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

  @Given("^the user is on the vehicle lookup page$")
  def the_user_is_on_the_vehicle_lookup_page()  {
    goToVehicleLookUpPage()
    page.title shouldEqual VehicleLookupPage.title
  }

  @When("^the user submits vehicles details$")
  def the_user_submits_vehicles_details()  {
    VehicleLookupPage.vehicleRegistrationNumber enter RandomVrmGenerator.uniqueVrm
    VehicleLookupPage.documentReferenceNumber enter "88888888881"
    click on VehicleLookupPage.vehicleSoldToBusiness
    click on VehicleLookupPage.next
  }

  @Then("^the user will see \"(.*?)\" screen$")
  def the_user_will_see_screen(unSuccessfulText:String)  {
    page.source should include(unSuccessfulText)
  }

}
