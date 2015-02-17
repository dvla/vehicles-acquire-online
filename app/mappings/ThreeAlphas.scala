package mappings

import play.api.data.validation.{Valid, ValidationError, Invalid, Constraint}
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressAndPostcodeViewModel

object ThreeAlphas {

  val threeAlphasConstrain = Constraint[AddressAndPostcodeViewModel]("constraint.threealphas") {
    case model: AddressAndPostcodeViewModel =>
      if (model.addressLinesModel.buildingNameOrNumber.replaceAll( """[^A-Za-z]""", "").length < 3) 
        Invalid(ValidationError("error.threeAlphas"))
      else Valid
  }
}
