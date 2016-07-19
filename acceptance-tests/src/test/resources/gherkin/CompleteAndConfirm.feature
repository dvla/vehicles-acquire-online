@working
Feature:
  Background:
    Given the user is on the Complete and confirm page

# TODO uncomment these
#  Scenario: Complete and confirm - Confirm new keeper
#    When the user clicks the primary control labelled Confirm New Keeper
#    Then the user will be taken to the "Summary" page

#  Scenario: Complete and confirm - Back
#    When the user clicks the secondary control labelled Back
#    Then the user will be taken to the "Vehicle tax or SORN" page

  Scenario: Complete and confirm - Confirm new keeper with a date of sale before the date of disposal
    When the user enters a date of sale before the date of disposal and submits the form
    Then the user will remain on the complete and confirm page and a warning will be displayed
    And there will be an error message displayed "You’ve entered a date of sale that is before the previous keeper’s disposal date"
    Then the user confirms the transaction
    Then the user will be taken to the "Summary" page

  Scenario: Complete and Confirm - Confirm new keeper with a date of sale same as date of disposal
    When the user enters a date of sale same as date of disposal and submits the form
    Then the user will be taken to the "Summary" page

  Scenario: Complete and Confirm - Confirm new keeper with a date of sale after the date of disposal
    When the user enters a date of sale after the date of disposal and submits the form
    Then the user will be taken to the "Summary" page
