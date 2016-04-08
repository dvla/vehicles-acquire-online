@working
Feature:
  Background:
    Given the user is on the Vehicle lookup page with trade address from lookup

  Scenario: The user does not choose a new keeper type
    When the user navigates forwards from the vehicle lookup page and there are no validation errors
    Then the user remains on the VehicleLookPage

  Scenario: The user chooses a new keeper type of private
    When the user fills in the vrn, doc ref number and selects privateKeeper
    And the user navigates forwards from the vehicle lookup page and there are no validation errors
    Then the user is taken to the page entitled "Enter new keeper details"

  Scenario: The user chooses a new keeper type of business
    When the user fills in the vrn, doc ref number and selects businessKeeper
    And the user navigates forwards from the vehicle lookup page and there are no validation errors
    Then the user is taken to the page entitled "Enter new keeper details"

  Scenario: The user navigates to the previous page
    When the user selects the control labelled VehicleLookUpBack button
    Then the user is taken to the page entitled "Select trader address"

  Scenario: The user wishes to change the trader details
    When the user selects the 'Change these trader details?' function
    Then the user will be directed to the Provide trader details page with the entry fields empty

  Scenario: vehicle lookup fails because the document reference number returned from the search does not match
    When the user fills in data that results in document reference mismatch error from the micro service
    And the user performs the lookup
    Then the user will be redirected to the vehicle lookup failure page
    Then the page will contain text "Please check that the vehicle registration number and V5C document reference number are correct and select the ‘Try again’ button below."

  Scenario: vehicle lookup is performed on a vehicle that has not yet been disposed
    When the user fills in data for a vehicle which has not been disposed
    And the user performs the lookup
    Then the user is taken to the page entitled "A keeper is on the record"
