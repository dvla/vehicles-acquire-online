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

  def newBusinessKeeperBuysAVehicleFromTheTrade = {
    val data = csv("data/happy/NewBusinessKeeperBuysAVehicleFromTheTrade.csv").circular
    val chain = new Chains(data)
    scenario("New business keeper buys a vehicle from the motor trade from start to finish")
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

  def newBusinessKeeperBuysAVehicleFromTheTradeWithAllOptionalDataFilledIn = {
    val data = csv("data/happy/NewBusinessKeeperBuysAVehicleFromTheTradeWithAllOptionalDataFilledIn.csv").circular
    val chain = new Chains(data)
    val name = "New business keeper buys a vehicle from the motor trade with all optional data filled in " +
      "from start to finish"
    scenario(name)
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

  def newPrivateKeeperBuysAVehicleFromTheTrade = {
    val data = csv("data/happy/NewPrivateKeeperBuysAVehicleFromTheTrade.csv").circular
    val chain = new Chains(data)
    scenario("New private keeper buys a vehicle from the motor trade from start to finish")
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

  def newPrivateKeeperBuysAVehicleFromTheTradeWithAllOptionalDataFilledIn = {
    val data = csv("data/happy/NewPrivateKeeperBuysAVehicleFromTheTradeWithAllOptionalDataFilledIn.csv").circular
    val chain = new Chains(data)
    val name = "New private keeper buys a vehicle from the motor trade with all optional data filled in " +
      "from start to finish"
    scenario(name)
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
