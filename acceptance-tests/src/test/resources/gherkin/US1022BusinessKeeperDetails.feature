@working
Feature:
  Background:
    Given the user is on the Enter business keeper details page

  Scenario: Enter business keeper details - Next
    When the user navigates forwards from the business keeper details page and there are no validation errors
    Then the user is taken to the page entitled "Select new keeper address"

  Scenario: Enter business keeper details - Back
    When the user navigates backwards from the business keeper details page
    Then the user is taken to the page entitled "Enter vehicle details"

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
