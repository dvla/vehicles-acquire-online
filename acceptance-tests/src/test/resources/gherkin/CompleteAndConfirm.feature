@working
Feature:
  Background:
    Given the user is on the Complete and confirm page

#  Scenario: Complete and confirm - Confirm new keeper
#    When the user clicks the primary control labelled Confirm New Keeper
#    Then the user will be taken to the "Summary" page

#  Scenario: Complete and confirm - Back
#    When the user clicks the secondary control labelled Back
#    Then the user will be taken to the "Vehicle tax or SORN" page

  Scenario: Date of sale - Invalid or incomplete
    When the user enters an invalid date of sale and submits the form
    Then there will be an error message displayed "Date of sale - Must be a valid date DD MM YYYY and not be in the future."

  Scenario: Date of sale - In the past
    When the user enters a date of sale in the past and submits the form
    Then there will be an error message displayed "Date of sale - We cannot accept a date of sale more than 5 years in the past. Please check and enter the correct date. If the date is correct then please submit the transaction via post."

  Scenario: Date of sale - In the future
    When the user enters a date of sale in the future and submits the form
    Then there will be an error message displayed "Date of sale - Must be a valid date DD MM YYYY and not be in the future."

  Scenario: Complete and confirm - Confirm new keeper with a date of sale before the date of disposal
    When the user enters a date of sale before the date of disposal and submits the form
    Then the user will remain on the complete and confirm page and a warning will be displayed
    Then the user confirms the transaction
    Then the user will be taken to the "Summary" page

  Scenario: Complet and Confirm - Confirm new keeper with a date of sale same as date of disposal
    When the user enters a date of sale same as date of disposal and submits the form
    Then the user will be taken to the "Summary" page

  Scenario: Complet and Confirm - Confirm new keeper with a date of sale after the date of disposal
    When the user enters a date of sale after the date of disposal and submits the form
    Then the user will be taken to the "Summary" page
