package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.WebDriver
import org.scalatest.Matchers
import pages.acquire.CompleteAndConfirmPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{WebBrowserDriver, WebBrowserDSL}

class CompleteAndConfirmSteps(webBrowserDriver: WebBrowserDriver) extends ScalaDsl with EN with WebBrowserDSL with Matchers {

  implicit val webDriver = webBrowserDriver.asInstanceOf[WebDriver]

  lazy val happyPath = new HappyPathSteps(webBrowserDriver)

  @Given("^the user is on the Complete and confirm page$")
  def the_user_is_on_the_complete_and_confirm_page() {
    happyPath.goToCompleteAndConfirmPage()
  }

  @Given("^the user is on the Complete and confirm page having entered transaction failure data$")
  def the_user_is_on_the_complete_and_confirm_page_having_entered_transaction_failure_data() {
    happyPath.goToCompleteAndConfirmPageWithTransactionFailureData()
  }

  @When("^the user clicks the primary control labelled Confirm New Keeper$")
  def the_user_clicks_the_primary_control_labelled_confirm_new_keeper() {
    happyPath.goToAcquireSuccessPage()
  }

  @When("^the user clicks the secondary control labelled Back$")
  def the_user_clicks_the_secondary_control_labelled_back() {
    happyPath.goToCompleteAndConfirmPageAndNavigateBackwards
  }

  @When("^the user confirms the transaction$")
  def the_user_confirms_the_transaction() = {
    click on CompleteAndConfirmPage.next
  }


  @Then("^the user will be taken to the \"(.*?)\" page$")
  def the_user_will_be_taken_to_the_page(pageTitle: String) {
     page.title should equal(pageTitle)
  }
}
