@working
Feature:
  Background:
    Given the user is on the Enter business keeper details page

  Scenario: Enter business keeper details - Next
    When the user navigates forwards from the business keeper details page and there are no validation errors
    Then the user is taken to the page entitled "Select new keeper address"

  Scenario: Enter business keeper details - Back
    When the user navigates backwards from the business keeper details page
    Then the user is taken to the page entitled "Enter vehicle details"
