package uk.gov.dvla.vehicles.acquire.gatling

import io.gatling.core.Predef._
import uk.gov.dvla.vehicles.acquire.gatling.Helper.httpConf
import uk.gov.dvla.vehicles.acquire.gatling.Scenarios._

class AcquireSimulation extends Simulation {

  private val oneUser = atOnceUsers(1)

  setUp(
    verifyAssetsAreAccessible.inject(oneUser),
    buyFromTraderForBusiness.inject(oneUser),
    buyFromTraderForBusinessAllOptionalDataFilledIn.inject(oneUser),
    buyFromTheTraderForPrivate.inject(oneUser),
    buyFromTraderForPrivateAllOptionalDataFilledIn.inject(oneUser),
    vehicleLookupUnsuccessful.inject(oneUser)
  ).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
