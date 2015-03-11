@working
Feature:
  Background:
    Given the user is on the Complete and confirm page having entered transaction failure data

  Scenario: Complete and confirm - Next
    When the user confirms the transaction
    Then the user will be taken to the "Summary" page
