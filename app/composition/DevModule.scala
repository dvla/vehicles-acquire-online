package composition

import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common
import common.ConfigProperties.getProperty
import common.clientsidesession.AesEncryption
import common.clientsidesession.ClearTextClientSideSessionFactory
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieEncryption
import common.clientsidesession.CookieFlags
import common.clientsidesession.CookieFlagsFromConfig
import common.clientsidesession.CookieNameHashGenerator
import common.clientsidesession.EncryptedClientSideSessionFactory
import common.clientsidesession.Sha1HashGenerator
import common.filters.AccessLoggingFilter.AccessLoggerName
import common.services.DateService
import common.services.DateServiceImpl
import common.webserviceclients.addresslookup.{AddressLookupService, AddressLookupWebService}
import common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl
import common.webserviceclients.addresslookup.ordnanceservey.WebServiceImpl
import common.webserviceclients.vehiclelookup.VehicleLookupServiceImpl
import common.webserviceclients.vehiclelookup.VehicleLookupService
import common.webserviceclients.vehiclelookup.VehicleLookupWebServiceImpl
import common.webserviceclients.vehiclelookup.VehicleLookupWebService
import common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import common.webserviceclients.bruteforceprevention.BruteForcePreventionServiceImpl
import common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService
import uk.gov.dvla.vehicles.presentation.common.filters.{DateTimeZoneServiceImpl, DateTimeZoneService}
import webserviceclients.acquire.{AcquireWebService, AcquireService, AcquireServiceImpl, AcquireWebServiceImpl}

/**
 * Provides real implementations of traits
 * Note the use of sse-guice, which is a library that makes the Guice internal DSL more scala friendly
 * eg we can write this:
 * bind[Service].to[ServiceImpl].in[Singleton]
 * instead of this:
 * bind(classOf[Service]).to(classOf[ServiceImpl]).in(classOf[Singleton])
 *
 * Look in build.scala for where we import the sse-guice library
 */
object DevModule extends ScalaModule {
  def configure() {
    bind[AddressLookupService].to[AddressLookupServiceImpl].asEagerSingleton()
    bind[AddressLookupWebService].to[WebServiceImpl].asEagerSingleton()

    bind[DateService].to[DateServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[CookieFlagsFromConfig].asEagerSingleton()

    if (getProperty("encryptCookies", default = true)) {
      bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
      bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
      bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
    } else
      bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[VehicleLookupWebService].to[VehicleLookupWebServiceImpl].asEagerSingleton()
    bind[VehicleLookupService].to[VehicleLookupServiceImpl].asEagerSingleton()

    bind[AcquireWebService].to[AcquireWebServiceImpl].asEagerSingleton()
    bind[AcquireService].to[AcquireServiceImpl].asEagerSingleton()

    bind[BruteForcePreventionWebService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.WebServiceImpl].asEagerSingleton()
    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()

    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.pages.common.AccessLogger"))
    bind[DateTimeZoneService].toInstance(new DateTimeZoneServiceImpl)
  }
}