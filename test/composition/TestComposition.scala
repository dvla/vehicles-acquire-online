package composition

import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}
import com.tzavellas.sse.guice.ScalaModule
import org.scalatest.mock.MockitoSugar
import play.api.Logger
import uk.gov.dvla.vehicles.presentation.common.filters.{DateTimeZoneServiceImpl, DateTimeZoneService}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire.{AcquireConfig, AcquireWebService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.acquire_service.FakeAcquireConfig
import utils.helpers.Config
import webserviceclients.fakes.FakeAcquireWebServiceImpl
import webserviceclients.fakes.FakeDateServiceImpl
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupService, VehicleAndKeeperLookupWebServiceImpl, VehicleAndKeeperLookupWebService}
import webserviceclients.fakes._
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CookieFlags
import common.clientsidesession.NoCookieFlags
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.ClearTextClientSideSessionFactory
import common.webserviceclients.addresslookup.{AddressLookupWebService, AddressLookupService}
import common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import webserviceclients.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl

trait TestComposition extends Composition {
  override lazy val injector: Injector = Guice.createInjector(testMod)

  private def testMod = Modules.`override`(new DevModule {
    override protected def bindClientSideSessionFactory(): Unit = ()
  }).`with`(new TestModule())

  def testModule(module: Module*) = Modules.`override`(testMod).`with`(module: _*)
  def testInjector(module: Module*) = Guice.createInjector(testModule(module: _*))
}

/**
 * Bind the fake implementations the traits
 */
private class TestModule() extends ScalaModule with MockitoSugar {
  def configure() {
    Logger.debug("Guice is loading TestModule")

    bind[AcquireConfig].to[FakeAcquireConfig]
    bind[utils.helpers.Config].toInstance(new TestConfig)

    ordnanceSurveyAddressLookup()
    bind[VehicleAndKeeperLookupWebService].to[FakeVehicleAndKeeperLookupWebService].asEagerSingleton()

    bind[AcquireWebService].to[FakeAcquireWebServiceImpl].asEagerSingleton()

    bind[DateService].to[FakeDateServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[BruteForcePreventionWebService].to[FakeBruteForcePreventionWebServiceImpl].asEagerSingleton()
  }

  private def ordnanceSurveyAddressLookup() = {
    bind[AddressLookupService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl]

    val fakeWebServiceImpl = new FakeAddressLookupWebServiceImpl(
      responseOfPostcodeWebService = FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress,
      responseOfUprnWebService = FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
    )
    bind[AddressLookupWebService].toInstance(fakeWebServiceImpl)
    bind[DateTimeZoneService].toInstance(new DateTimeZoneServiceImpl)
  }
}
