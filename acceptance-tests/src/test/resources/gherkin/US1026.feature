@tag
Feature:
  Background:
    Given the user is on the "Complete and confirm" page

  Scenario:Complete and confirm - Confirm New Keeper
    When the user click on  the primary control labelled "Confirm New Keeper"
    Then the user will be taken to the Summary page

  Scenario: Complete and confirm - Back
    When the user selects the secondary control labelled "Back"
    Then the user will be taken to the "Vehicle tax or SORN" page
