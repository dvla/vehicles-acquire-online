@working
Feature:
  Scenario: Date of sale - Invalid or incomplete
    Given the user is on the Complete and confirm page
    When the user enters an invalid date of sale and submits the form
    Then there will be an error message displayed "Date of sale - Must be a valid date DD MM YYYY and not be in the future."

  Scenario: Date of sale - In the past
    Given the user is on the Complete and confirm page
    When the user enters a date of sale over 5 years in the past and submits the form
    Then there will be an error message displayed "Date of sale - We cannot accept a date of sale more than 5 years in the past. Please check and enter the correct date. If the date is correct then please submit the transaction via post."

  Scenario: Date of sale - In the future
    Given the user is on the Complete and confirm page
    When the user enters a date of sale in the future and submits the form
    Then there will be an error message displayed "Date of sale - Must be a valid date DD MM YYYY and not be in the future."

  Scenario: Date of sale - Over 12 months in the past
    Given the user is on the Complete and confirm page with VRN "AA11AAR" and V5C "88888888881"
    When the user enters a date of sale over 12 months in the past and submits the form
    Then the user will remain on the complete and confirm page and a warning will be displayed
    And there will be an error message displayed "The date you have entered is over 12 months ago"
    Then the user confirms the transaction
    Then the user will be taken to the "Summary" page
