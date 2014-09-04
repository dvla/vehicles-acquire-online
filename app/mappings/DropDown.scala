package mappings

import play.api.data.Forms.nonEmptyText
import play.api.data.Mapping
import play.api.data.validation.{ValidationError, Invalid, Valid, Constraint}

object DropDown {
  def addressDropDown: Mapping[String] = nonEmptyText

  def titleDropDown(dropDownOptions: Seq[(String, String)]): Mapping[String] = nonEmptyText
}