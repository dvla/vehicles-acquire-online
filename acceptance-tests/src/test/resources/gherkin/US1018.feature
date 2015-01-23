@tag
Feature:
  Background:
    Given the user is on the Vehicle lookup page

  Scenario: The user does not choose a new keeper type
    When the user navigates to the next page
    Then the user remains on the VehicleLookPage

  Scenario: The user chooses a new keeper type of private
    When the user fills in the vrn, doc ref number and selects privateKeeper
    And the user navigates to the next page
    Then the user is taken to the page entitled "Enter new keeper details"

  Scenario: The user chooses a new keeper type of business
    When the user fills in the vrn, doc ref number and selects businessKeeper
    And the user navigates to the next page
    Then the user is taken to the page entitled "Enter business keeper details"

  Scenario: The user navigates to the previous page
    When the user selects the control labelled VehicleLookUpBack button
    Then the user is taken to the page entitled "Select trader address"
