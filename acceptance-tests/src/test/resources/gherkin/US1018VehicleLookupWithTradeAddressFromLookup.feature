@working
Feature:
  Background:
    Given the user is on the Vehicle lookup page with trade address from lookup

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

  Scenario: vehicle lookup fails because the vrn is not found
    When the user fills in data that results in vrn not found error from the micro service
    And the user performs the lookup
    Then the user will be redirected to the vehicle lookup failure page
    Then the page will contain text "The V5C document reference number entered is either not valid or does not come from the most recent V5C issued for this vehicle."

  Scenario: vehicle lookup fails because the document reference number returned from the search does not match
    When the user fills in data that results in document reference mismatch error from the micro service
    And the user performs the lookup
    Then the user will be redirected to the vehicle lookup failure page
    Then the page will contain text "The V5C document reference number entered is either not valid or does not come from the most recent V5C issued for this vehicle."

  Scenario: vehicle lookup is performed on a vehicle that has not yet been disposed
    When the user fills in data for a vehicle which has not been disposed
    And the user performs the lookup
    Then the user is taken to the page entitled "A keeper is on the record"
