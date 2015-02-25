package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.java.en.{Then, When, Given}
import cucumber.api.scala.{EN, ScalaDsl}
import org.joda.time.DateTime
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
    happyPath.goToCompleteAndConfirmPageAndNavigateBackwards()
  }

  @When("^the user confirms the transaction$")
  def the_user_confirms_the_transaction() = {
    click on CompleteAndConfirmPage.next
  }

  @When("^the user enters an invalid date of sale and submits the form$")
  def the_user_enters_an_invalid_date_of_sale_and_submits_the_form() = {
    happyPath.fillInCompleteAndConfirm(year = "201")
    the_user_confirms_the_transaction()
  }

  @When("^the user enters a date of sale in the future and submits the form$")
  def the_user_enters_a_date_of_sale_in_the_future_and_submits_the_form() = {
    val nextYear = DateTime.now().plusDays(365)
    happyPath.fillInCompleteAndConfirm(year = nextYear.getYear.toString)
    the_user_confirms_the_transaction()
  }

  @When("^the user enters a date of sale before the date of disposal and submits the form$")
  def the_user_enters_a_date_of_sale_before_the_date_of_disposal_and_submits_the_form() = {
    happyPath.fillInCompleteAndConfirm(year = "2013")
    the_user_confirms_the_transaction()
  }

  @Then("^the user will be taken to the \"(.*?)\" page$")
  def the_user_will_be_taken_to_the_page(pageTitle: String) = {
    page.title should include(pageTitle)
  }

  @Then("^the user will remain on the complete and confirm page and a warning will be displayed$")
  def the_user_will_be_taken_to_the_page() = {
    page.title should equal(CompleteAndConfirmPage.title)
    page.source should include("<div class=\"popup-modal\">")
  }
  
  @When("^the user enters a date of sale same as date of disposal and submits the form$")
  def the_user_enters_a_date_of_sale_same_as_date_of_disposal_and_submits_the_form()  {
    happyPath.fillInCompleteAndConfirm(day = "29", month = "12", year = "2014")
    the_user_confirms_the_transaction()
  }

  @When("^the user enters a date of sale after the date of disposal and submits the form$")
  def the_user_enters_a_date_of_sale_after_the_date_of_disposal_and_submits_the_form()  {
    happyPath.fillInCompleteAndConfirm(day = "30", month = "12", year = "2014")
    the_user_confirms_the_transaction()
  }

}
