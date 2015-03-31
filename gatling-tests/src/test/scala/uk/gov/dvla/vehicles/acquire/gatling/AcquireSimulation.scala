package uk.gov.dvla.vehicles.acquire.gatling

import io.gatling.core.Predef._
import uk.gov.dvla.vehicles.acquire.gatling.Helper.httpConf
import uk.gov.dvla.vehicles.acquire.gatling.Scenarios._

class AcquireSimulation extends Simulation {

  private val oneUser = atOnceUsers(1)

  setUp(
    //verifyAssetsAreAccessible.inject(oneUser),
    newBusinessKeeperBuysAVehicleFromTheTrade.inject(oneUser),
    newBusinessKeeperBuysAVehicleFromTheTradeWithAllOptionalDataFilledIn.inject(oneUser),
    newPrivateKeeperBuysAVehicleFromTheTrade.inject(oneUser),
    newPrivateKeeperBuysAVehicleFromTheTradeWithAllOptionalDataFilledIn.inject(oneUser),
    vehicleLookupUnsuccessful.inject(oneUser)
  ).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
