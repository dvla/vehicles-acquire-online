@working
Feature:
  Scenario: Unsuccessful message for System Audit Vehicles
    Given the user is on the vehicle lookup page
    When  the user submits vehicles details
    Then  the user will see "Look-up was unsuccessful" screen