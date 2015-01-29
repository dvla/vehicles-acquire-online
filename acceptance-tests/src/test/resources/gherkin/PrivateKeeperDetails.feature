@working
Feature:
  Background:
    Given the user is on the Private keeper details page

  Scenario: Enter private keeper details - Next
    When the user navigates forwards from private keeper details page and there are no validation errors
    Then the user is taken to the page entitled "Select new keeper address"

  Scenario: Enter private keeper details - Back
    When the user navigates backwards from private keeper details page
    Then the user is taken to the page entitled "Enter vehicle details"
