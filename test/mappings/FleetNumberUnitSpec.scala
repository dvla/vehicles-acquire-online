package mappings

import helpers.UnitSpec

final class FleetNumberUnitSpec extends UnitSpec {

  /**
   * Test valid fleet number formats
   */
  val validFleetNumbers = Seq("123456", "12345-")
  validFleetNumbers.map(fleetNumber => s"indicate the fleet number is valid: $fleetNumber" in {
    val result = isFleetNumber(fleetNumber)
    result should equal(true)
  })

  /**
   * Test invalid fleet number formats
   */
  val invalidFleetNumbers = Seq("", "1234567", "-12345", "123-45", "A")
  invalidFleetNumbers.map(fleetNumber => s"indicate the fleet number is not valid: $fleetNumber" in {
    val result = isFleetNumber(fleetNumber)
    result should equal(false)
  })

  private def isFleetNumber(mileage: String): Boolean = {
    val regex = FleetNumber.Pattern.r
    if (regex.pattern.matcher(mileage).matches) true
    else false
  }
}
