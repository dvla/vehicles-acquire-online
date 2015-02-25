@working
Feature: Suppression Flag

  Scenario:  Supression flag = 's'
    Given the user is in vehicle lookup page
    When  the user  enter the vehicle details which has a supression flag and click on submit
    Then the user is taken to the page entitled "Vehicle is part of a suppressed fleet"

  Scenario: Supression flag = 'null or x'
    Given the user is in vehicle lookup page
    When  the user enters the vehicle details which does not have a supression flag and click on Next button without any validation errors
    Then  the user will successfully navigate to the next page