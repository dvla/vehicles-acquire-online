@working
Feature:

  Scenario: Complete and confirm - Confirm new keeper
    When the user clicks the primary control labelled Confirm New Keeper
    Then the user will be taken to the "Summary" page

  Scenario: Complete and confirm - Back
    Given the user is on the Complete and confirm page
    When the user clicks the secondary control labelled Back
    Then the user will be taken to the "Vehicle tax or SORN" page

  Scenario: Complete and Confirm - Invalid or incomplete
    Given the user is on the Complete and confirm page
    When the user enters an invalid date of sale and submits the form
    Then there will be an error message displayed "Date of sale - Must be a valid date DD MM YYYY and not be in the future."

  Scenario: Complete and Confirm - In the past
    Given the user is on the Complete and confirm page
    When the user enters a date of sale over 5 years in the past and submits the form
    Then there will be an error message displayed "Date of sale - We cannot accept a date of sale more than 5 years in the past. Please check and enter the correct date. If the date is correct then please submit the transaction via post."

  Scenario: Complete and Confirm - In the future
    Given the user is on the Complete and confirm page
    When the user enters a date of sale in the future and submits the form
    Then there will be an error message displayed "Date of sale - Must be a valid date DD MM YYYY and not be in the future."

  Scenario: Complete and Confirm - Over 12 months in the past
    Given the user is on the Complete and confirm page with VRN "AA11AAR" and V5C "88888888881"
    When the user enters a date of sale over 12 months in the past and submits the form
    Then the user will remain on the complete and confirm page and a warning will be displayed
    And there will be an error message displayed "The date you have entered is over 12 months ago"
    Then the user confirms the transaction
    Then the user will be taken to the "Summary" page

  Scenario: Complete and confirm - Confirm new keeper with a date of sale before the date of disposal
    Given the user is on the Complete and confirm page
    When the user enters a date of sale before the date of disposal and submits the form
    Then the user will remain on the complete and confirm page and a warning will be displayed
    And there will be an error message displayed "The date you’ve entered is before the current keeper acquired the vehicle, which is shown on the V5C registration certificate (logbook). Please enter a correct date that’s later than the one shown on the logbook and select ‘Next’."
    Then the user enters a valid date and confirms the transaction
    Then the user will be taken to the "Summary" page

  Scenario: Complete and Confirm - Confirm new keeper with a date of sale same as date of disposal
    Given the user is on the Complete and confirm page
    When the user enters a date of sale same as date of disposal and submits the form
    Then the user will be taken to the "Summary" page

  Scenario: Complete and Confirm - Confirm new keeper with a date of sale after the date of disposal
    Given the user is on the Complete and confirm page
    When the user enters a date of sale after the date of disposal and submits the form
    Then the user will be taken to the "Summary" page
