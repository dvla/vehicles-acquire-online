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

  Scenario: Date of birth - Invalid or incomplete
    When the user enters an invalid date of birth and submits the form
    Then there will be an error message displayed "Date of birth of new keeper - Must be a valid date DD MM YYYY and not be in the future."

  Scenario: Date of birth - More than 110 years in the past
    When the user enters a date of birth more than 110 years in the past and submits the form
    Then there will be an error message displayed "Date of birth of new keeper - Date of birth cannot be more than 110 years in the past"

  Scenario: Date of birth - In the future
    When the user enters a date of birth in the future and submits the form
    Then there will be an error message displayed "Date of birth of new keeper - Must be a valid date DD MM YYYY and not be in the future."
