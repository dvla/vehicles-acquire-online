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

  Scenario: First name - Invalid
    When the user enters an invalid first name and submits the form
    Then there will be an error message displayed "First name - Must contain between 1 and 25 characters from the following A-Z, hyphen, apostrophe, full stop and space. The following characters cannot be used at the start of the first name (hyphen, apostrophe, full stop and space)"

  Scenario: Last name - Invalid
    When the user enters an invalid last name and submits the form
    Then there will be an error message displayed "Last name - Must contain between 1 and 25 characters from the following A-Z, hyphen, apostrophe, full stop and space"

  Scenario: Date of birth - Invalid or incomplete
  Scenario: Date of birth - Invalid or incomplete
    When the user enters an invalid date of birth and submits the form
    Then there will be an error message displayed "Date of Birth - Must be a valid date DD MM YYYY and not be in the future."

  Scenario: Date of birth - More than 110 years in the past
    When the user enters a date of birth more than 110 years in the past and submits the form
    Then there will be an error message displayed "Date of Birth - Date of birth cannot be more than 110 years in the past"

  Scenario: Date of birth - In the future
    When the user enters a date of birth in the future and submits the form
    Then there will be an error message displayed "Date of Birth - Must be a valid date DD MM YYYY and not be in the future."
