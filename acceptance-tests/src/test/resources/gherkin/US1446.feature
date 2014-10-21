@tag
Feature: Minimum Happy Path Acceptance Tests For VehicleAcquireOnline
  Background:
    Given the user is on the Provide trader details page

  Scenario:Private Keeper Happy Path
    When the trader entered through successful postcode lookup
    And entered valid registration number and doc reference number
    And the user on Private Keeper details page and entered through successful postcode lookup
    Then the user will be on confirmed summary page

  Scenario:Business Keeper Happy Path
    When the trader entered through successful postcode lookup.
    And entered valid registration number and doc reference number
    And the user on Business Keeper details page and entered through successful postcode lookup
    Then the user will be on confirmed summary page.

  Scenario:Business Keeper Happy Path with unsuccessful postcode
    When the trader entered through unsuccessful postcode lookup
    And entered valid registration number and doc reference number
    And the user on Business Keeper details page and entered through unsuccessful postcode lookup
    Then the user will be on confirmed summary page.

  Scenario:Private Keeper Happy Path with unsuccessful postcode
    When the trader entered through unsuccessful postcode lookup
    And entered valid registration number and doc reference number
    And the user on Private Keeper details page and entered through unsuccessful postcode lookup
    Then the user will be on confirmed summary page.


  Scenario:Private Keeper Happy Path with unsuccessful postcode
    When the trader entered through unsuccessful postcode lookup
    And entered valid registration number and doc reference number
    And the user on Private Keeper details page and entered through unsuccessful postcode lookup
    Then the user will be on confirmed transaction failure screen


