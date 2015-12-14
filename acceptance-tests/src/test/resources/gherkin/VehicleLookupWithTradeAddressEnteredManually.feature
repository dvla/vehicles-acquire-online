@working
Feature:
  Background:
    Given the user is on the Vehicle lookup page with trade address entered manually

  Scenario: The user navigates to the previous page
    When the user selects the control labelled VehicleLookUpBack button
    Then the user is taken to the page entitled "Enter address"

  Scenario: The user wishes to change the trader details
    When the user selects the 'Change these trader details?' function
    Then the user will be directed to the Provide trader details page with the entry fields empty
