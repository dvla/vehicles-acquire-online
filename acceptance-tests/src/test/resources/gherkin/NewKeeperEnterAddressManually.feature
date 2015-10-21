@working
Feature:
  Background:
    Given the user is on the New keeper enter address manually page

  Scenario: New keeper enter address manually - Next
    When the user navigates forwards from new keeper enter address manually and there are no validation errors
    Then the user is taken to the page entitled "Vehicle tax or SORN"

  Scenario: New keeper enter address manually with changed post code - Next
    When the user changes the postcode to "SA1 1AA" and navigates forwards from new keeper enter address manually and there are no validation errors
    Then the user is taken to the page entitled "Vehicle tax or SORN"
    And the page will contain text "SA1 1AA"

  Scenario: New keeper enter address manually - Back
    When the user navigates backwards from the new keeper enter address manually page
    Then the user is taken to the page entitled "Select new keeper address"
