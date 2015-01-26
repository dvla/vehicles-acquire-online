@working
Feature:
  Background:
    Given the user is on the Enter business keeper details page

  Scenario: Enter business keeper details - Next
# when the user navigates to the next page and there are no validation errors
    When the user selects the primary control labelled Next and there are no validation errors
    Then the user is taken to the page entitled "Select new keeper address"

  Scenario: Enter business keeper details - Back
# when the user navigates to the previous page
    When the user selects the secondary control labelled BusinessKeeperBack button
    Then the user is taken to the page entitled "Enter vehicle details"
