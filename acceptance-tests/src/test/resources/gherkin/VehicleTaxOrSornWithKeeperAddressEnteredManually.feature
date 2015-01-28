@working
Feature:
  Background:
    Given the user is on the Vehicle tax or sorn page with keeper address entered manually

  Scenario: Navigate to the next page
    When the user navigates forwards from the vehicle tax or sorn page and there are no validation errors
    Then the user is taken to the page entitled "Complete and confirm"

  Scenario: Navigate to the previous page after manually entering new keeper address
    When the user navigates backwards from the vehicle tax or sorn page
    Then the user is taken to the page entitled "Enter keeper address"
