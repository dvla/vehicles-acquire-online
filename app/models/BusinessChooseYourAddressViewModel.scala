package models

import mappings.DropDown
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class BusinessChooseYourAddressViewModel(uprnSelected: String)

object BusinessChooseYourAddressViewModel {
  implicit val JsonFormat = Json.format[BusinessChooseYourAddressViewModel]
  final val BusinessChooseYourAddressCacheKey = "businessChooseYourAddress"
  implicit val Key = CacheKey[BusinessChooseYourAddressViewModel](value = BusinessChooseYourAddressCacheKey)

  object Form {
    final val AddressSelectId = "disposal_businessChooseYourAddress_addressSelect"
    final val Mapping = mapping(
      AddressSelectId -> DropDown.addressDropDown
    )(BusinessChooseYourAddressViewModel.apply)(BusinessChooseYourAddressViewModel.unapply)
  }
}
