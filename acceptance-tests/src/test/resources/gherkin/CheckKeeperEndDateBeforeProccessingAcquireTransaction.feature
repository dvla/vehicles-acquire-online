@working
 Feature:Checking KeeperEndDate before proccesing Acquire Transaction

  Background:
    Given the user is on the Vehicle LookUp page

  Scenario:
    When  the user has submitted a vehicle lookup request and a matching record is returned
    Then  the user is presented with an error message "A keeper is on the record"

  Scenario:
    When the user has submitted a vehicle lookup request which does n't have keeper end date
    Then the user will navigate to next page successfully

