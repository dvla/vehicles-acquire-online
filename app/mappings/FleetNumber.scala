package mappings

import play.api.data.Forms.{optional, text}
import play.api.data.Mapping

object FleetNumber {
  final val MinLength = 6
  final val MaxLength = 6
  // This pattern is being used directly in the view when configuring the ValtechInputText widget
  final val Pattern = """^[0-9]{6}|[0-9]{5}\-$"""

  def fleetNumberMapping: Mapping[Option[String]] = optional(text(minLength = MinLength, maxLength = MaxLength))
}
