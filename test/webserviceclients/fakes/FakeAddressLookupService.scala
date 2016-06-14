package webserviceclients.fakes

import pages.acquire.SetupTradeDetailsPage.PostcodeValid
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

object FakeAddressLookupService {
  val addressWithoutUprn = AddressModel(address = Seq("44 Hythe Road", "White City", "London", PostcodeValid))
  final val BuildingNameOrNumberValid = "123ABC"
  final val Line2Valid = "line2 stub"
  final val Line3Valid = "line3 stub"
  final val PostTownValid = "postTown stub"

}