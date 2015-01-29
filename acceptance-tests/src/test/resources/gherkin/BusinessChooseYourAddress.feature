@working
Feature:
  Background:
    Given the user is on the Business choose your address page

  Scenario: Business choose your address - Next
    When the user navigates forwards from business choose your address page and there are no validation errors
    Then the user is taken to the page entitled "Enter vehicle details"

  Scenario: Business choose your address - choose to enter address manually
    When the user navigates forwards from business choose your address page to the enter address manually page
    Then the user is taken to the page entitled "Enter address"

  Scenario: Business choose your address - Back
    When the user navigates backwards from the business choose your address page
    Then the user is taken to the page entitled "Provide trader details"
