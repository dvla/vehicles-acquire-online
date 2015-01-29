@working
Feature:
  Background:
    Given the user is on the Enter address manually page

  Scenario: Enter address manually - Next
    When the user navigates forwards from enter address manually and there are no validation errors
    Then the user is taken to the page entitled "Enter vehicle details"

  Scenario: Enter address manually - Back
    When the user navigates backwards from the enter address manually page
    Then the user is taken to the page entitled "Select trader address"
