@workingBroken
Feature: Minimum happy path acceptance tests for VehiclesAcquireOnline
  Background:
    Given the user is on the Vehicle lookup page

  Scenario: The user is on the vehicle lookup page and the user chooses private keeper type upon performing the search the user is taken to the private keeper details page
    When the user chooses private keeper and performs the vehicle lookup
    Then the user is taken to the Private Keeper details page

#  Scenario: Private keeper happy path
#    When the trader entered through successful postcode lookup
#    And entered valid registration number and doc reference number
#    And the user on Private Keeper details page and entered through successful postcode lookup
#    Then the user will be on confirmed summary page

#  Scenario: Business keeper happy path through the application finishing on the acquire success page
#    When the trader entered through successful postcode lookup
#    And entered valid registration number and doc reference number
#    And the user on Business Keeper details page and entered through successful postcode lookup
#    Then the user will be on confirmed summary page

#  Scenario: Business keeper happy path with unsuccessful postcode
#    When the trader entered through unsuccessful postcode lookup
#    And entered valid registration number and doc reference number
#    And the user on Business Keeper details page and entered through unsuccessful postcode lookup
#    Then the user will be on confirmed summary page

#  Scenario: Private keeper happy path with unsuccessful postcode
#    When the trader entered through unsuccessful postcode lookup
#    And entered valid registration number and doc reference number
#    And the user on Private Keeper details page and entered through unsuccessful postcode lookup
#    Then the user will be on confirmed summary page

#  Scenario: Private keeper happy path with unsuccessful postcode failure screen
#    When the trader entered through unsuccessful postcode lookup
#    And entered valid registration number and doc reference number
#    And the user on Private Keeper details page and entered through unsuccessful postcode lookup with failure data
#    Then the user will be on confirmed transaction failure screen
