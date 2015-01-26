@working
Feature:
  Background:
    Given the user is on the Vehicle lookup page

  Scenario: vehicle lookup fails because the vrm is not found
    When the user fills in data that results in vrn not found error from the micro service
    And the user performs the lookup
    Then the user will be redirected to the vehicle lookup failure page
    Then the page will contain text "The V5C document reference number entered is either not valid or does not come from the most recent V5C issued for this vehicle."

  Scenario: vehicle lookup fails because the document reference number returned from the search does not match
    When the user fills in data that results in document reference mismatch error from the micro service
    And the user performs the lookup
    Then the user will be redirected to the vehicle lookup failure page
    Then the page will contain text "The V5C document reference number entered is either not valid or does not come from the most recent V5C issued for this vehicle."
