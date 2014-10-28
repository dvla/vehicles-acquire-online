package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.webbrowser.{WebBrowserDriver, WebBrowserDSL}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire._

final class CompleteAndConfirm(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {


  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  lazy val vaHappyPath = new VAHappyPath(webBrowserDriver)

  @Given("^the user is on the \"(.*?)\" page$")
  def the_user_is_on_the_page(confirm: String) {
    vaHappyPath.fillInVehicleDetailsPage()
  }

  @When("^the user click on  the primary control labelled \"(.*?)\"$")
  def the_user_click_on_the_primary_control_labelled(confirmPage: String) {
    vaHappyPath.fillInPrivateKeeperDetailsPage()
    vaHappyPath.navigateAfterCustmourTypeSelection(true)
  }

  @Then("^the user will be taken to the Summary page$")
  def the_user_will_be_taken_to_the_Summary_page() {
    page.title should equal(AcquireSuccessPage.title)
  }

  @When("^the user selects the secondary control labelled \"(.*?)\"$")
  def the_user_selects_the_secondary_control_labelled(back: String) {
    vaHappyPath.fillInPrivateKeeperDetailsPage()
    vaHappyPath.navigateAfterCustmourTypeSelection(false)
  }

  @Then("^the user will be taken to the \"(.*?)\" page$")
  def the_user_will_be_taken_to_the_page(sorn: String) {
     page.title should equal(sorn)
  }


}
