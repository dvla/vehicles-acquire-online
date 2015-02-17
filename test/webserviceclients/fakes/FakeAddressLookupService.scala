package webserviceclients.fakes

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.{UprnValid, UprnValid2}

object FakeAddressLookupService {
  final val TraderBusinessNameValid = "example trader name"
  final val PostcodeWithoutAddresses = "xx99xx"
  final val PostcodeValid = "QQ99QQ"
  val addressWithoutUprn = AddressModel(address = Seq("44 Hythe Road", "White City", "London", PostcodeValid))
  val addressWithUprn = AddressModel(
    uprn = Some(UprnValid),
    address = Seq("44 Hythe Road", "White City", "London", PostcodeValid)
  )
  final val BuildingNameOrNumberValid = "123ABC"
  final val Line2Valid = "line2 stub"
  final val Line3Valid = "line3 stub"
  final val PostTownValid = "postTown stub"

  final val PostcodeValidWithSpace = "QQ9 9QQ"
  val fetchedAddresses = Seq(
    UprnValid.toString -> addressWithUprn.address.mkString(", "),
    UprnValid2.toString -> addressWithUprn.address.mkString(", ")
  )
}