package uk.gov.dvla.vehicles.acquire.gatling

import io.gatling.core.Predef._
import io.gatling.core.feeder._

object Scenarios {

  def verifyAssetsAreAccessible = {
    val noData = RecordSeqFeederBuilder[String](records = IndexedSeq.empty[Record[String]])
    val chain = new Chains(noData)
    scenario("Verify assets are accessible")
      .exec(
        chain.verifyAssetsAreAccessible
      )
  }

  def buyFromTraderForBusiness = {
    val data = csv("data/happy/BuyAVehicleFromTheTraderToBusiness.csv").circular
    val chain = new Chains(data)
    scenario("Buy a vehicle from the motor trader as a new business keeper from start to finish")
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.traderDetailsPage,
          chain.traderDetailsSubmit,
          chain.businessChooseYourAddress,
          chain.vehicleLookup,
          chain.vehicleLookupSubmitNewBusinessKeeper,
          chain.businessKeeperDetailsSubmit,
          chain.newKeeperChooseYourAddressSubmit,
          chain.taxOrSornVehicle,
          chain.completeAndConfirmSubmit
        )
      )
  }

  def buyFromTraderForBusinessAllOptionalDataFilledIn = {
    val data = csv("data/happy/BuyAVehicleFromTheTraderToBusinessAllOptionalDataFilledIn.csv").circular
    val chain = new Chains(data)
    scenario("Buy a vehicle from the motor trader as a new business keeper from start to finish with all optional data filled in")
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.traderDetailsPage,
          chain.traderDetailsSubmit,
          chain.businessChooseYourAddress,
          chain.vehicleLookup,
          chain.vehicleLookupSubmitNewBusinessKeeper,
          chain.businessKeeperDetailsSubmit,
          chain.newKeeperChooseYourAddressSubmit,
          chain.taxOrSornVehicle,
          chain.completeAndConfirmSubmit
        )
      )
  }

  def buyFromTheTraderForPrivate = {
    val data = csv("data/happy/BuyAVehicleFromTheTraderToPrivate.csv").circular
    val chain = new Chains(data)
    scenario("Buy a vehicle from the motor trader as a private keeper from start to finish")
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.traderDetailsPage,
          chain.traderDetailsSubmit,
          chain.businessChooseYourAddress,
          chain.vehicleLookup,
          chain.vehicleLookupSubmitNewPrivateKeeper,
          chain.privateKeeperDetailsSubmit,
          chain.newKeeperChooseYourAddressSubmit,
          chain.taxOrSornVehicle,
          chain.completeAndConfirmSubmit
        )
      )
  }

  def buyFromTraderForPrivateAllOptionalDataFilledIn = {
    val data = csv("data/happy/BuyAVehicleFromTheTraderToPrivateAllOptionalDataFilledIn.csv").circular
    val chain = new Chains(data)
    scenario("Buy a vehicle from the motor trader as a private keeper from start to finish with all optional data filled in")
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.traderDetailsPage,
          chain.traderDetailsSubmit,
          chain.businessChooseYourAddress,
          chain.vehicleLookup,
          chain.vehicleLookupSubmitNewPrivateKeeper,
          chain.privateKeeperDetailsSubmit,
          chain.newKeeperChooseYourAddressSubmit,
          chain.taxOrSornVehicle,
          chain.completeAndConfirmSubmit
        )
      )
  }

  def vehicleLookupUnsuccessful = {
    val data = csv("data/sad/VehicleLookupUnsuccessful.csv").circular
    val chain = new Chains(data)
    scenario("Vehicle lookup is unsuccessful")
      .exitBlockOnFail(
        exec(
          chain.beforeYouStart,
          chain.traderDetailsPage,
          chain.traderDetailsSubmit,
          chain.businessChooseYourAddress,
          chain.vehicleLookup,
          chain.vehicleLookupUnsuccessfulSubmit
        )
      )
  }
}