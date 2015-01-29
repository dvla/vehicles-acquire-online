@working
Feature:
  Background:
    Given the user is on the New keeper choose your address page having selected a new private keeper

  Scenario: New keeper choose your address - Next
    When the user navigates forwards from new keeper choose your address page
    Then the user is taken to the page entitled "Vehicle tax or SORN"

  Scenario: New keeper choose your address - choose to enter address manually
    When the user navigates forwards from new keeper choose your address to the new keeper enter address manually page
    Then the user is taken to the page entitled "Enter keeper address"

  Scenario: New keeper choose your address - Back
    When the user navigates backwards from the new keeper choose your address
    Then the user is taken to the page entitled "Enter new keeper details"
