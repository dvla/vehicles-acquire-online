package constraints

import play.api.data.validation.Constraints.pattern

object FleetNumber {
  final val MinLength = 6
  final val MaxLength = 6
  // This pattern is being used directly in the view when configuring the ValtechInputText widget
  final val Pattern = """^[0-9]{6}|[0-9]{5}\-$"""

  val fleetNumber = pattern(
    regex = Pattern.r,
    name = "constraint.restricted.fleetNumber",
    error = "error.restricted.fleetNumber"
  )
}
