@tag
Feature:

  Scenario: Enter business keeper details - Next
    Given the user is on the Enter business keeper details page
    When the user selects the primary control labelled Next and there are no validation errors
    Then the user will be taken to the page titled "Select new keeper address" page

  Scenario:Enter business keeper details - Back
    Given the user is on the Enter business keeper details page
    When the user selects the secondary control labelled BusinessKeeperBack button
    Then the user will be taken to the page titled "Vehicle lookup" page
