package composition

import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import org.scalatest.mock.MockitoSugar
import play.api.{LoggerLike, Logger}
import webserviceclients.fakes.{FakeVehicleLookupWebService, FakeAddressLookupWebServiceImpl}
import uk.gov.dvla.vehicles.presentation.common
import common.webserviceclients.vehiclelookup.{VehicleLookupServiceImpl, VehicleLookupService, VehicleLookupWebService}
import common.clientsidesession.CookieFlags
import common.clientsidesession.NoCookieFlags
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.ClearTextClientSideSessionFactory
import common.filters.AccessLoggingFilter.AccessLoggerName
import common.webserviceclients.addresslookup.{AddressLookupWebService, AddressLookupService}

class TestModule() extends ScalaModule with MockitoSugar {
  /**
   * Bind the fake implementations the traits
   */
  def configure() {
    Logger.debug("Guice is loading TestModule")
    ordnanceSurveyAddressLookup()
    bind[VehicleLookupWebService].to[FakeVehicleLookupWebService].asEagerSingleton()
    bind[VehicleLookupService].to[VehicleLookupServiceImpl].asEagerSingleton()

//    bind[DateService].to[FakeDateServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.pages.common.AccessLogger"))
  }

  private def ordnanceSurveyAddressLookup() = {
    bind[AddressLookupService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl]

    val fakeWebServiceImpl = new FakeAddressLookupWebServiceImpl(
      responseOfPostcodeWebService = FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress,
      responseOfUprnWebService = FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
    )
    bind[AddressLookupWebService].toInstance(fakeWebServiceImpl)
  }
}
