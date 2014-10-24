package constraints

import play.api.data.validation.Constraint
import play.api.data.validation.Constraints._

object BusinessKeeperName {

  def validBusinessKeeperName: Constraint[String] = pattern(
    regex = """^[a-zA-Z0-9][a-zA-Z0-9\s\-\'\,]*$""".r,
    name = "constraint.validBusinessKeeperName",
    error = "error.validBusinessKeeperName")
}
