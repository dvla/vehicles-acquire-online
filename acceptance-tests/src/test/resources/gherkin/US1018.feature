@tag
Feature:
  Background:
    Given the user is on the Vehicle lookup page

  Scenario: Vehicle lookup disposal not set
    When the user selects the control labelled Next
    Then the user will be stay on the VehicleLookPage

  Scenario:Vehicle lookup disposal set private
    When the user fill the vrn doc ref number and select privateKeeper
    And the user selects the control labelled Next
    Then the user will be taken to the page titled "Enter new keeper details" page

  Scenario:Vehicle lookup - disposal set business
    When the user fill the vrn doc ref number and select businessKeeper
    And the user selects the control labelled Next
    Then the user will be taken to the page titled "Enter business keeper details" page

  Scenario:Vehicle lookup - Back
    When the user selects the  control labelled VehicleLookUpBack button
    Then the user will be taken to the page titled Select trader address page
