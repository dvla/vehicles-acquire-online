package composition

import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import org.scalatest.mock.MockitoSugar
import play.api.{LoggerLike, Logger}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.NoCookieFlags
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName

class TestModule() extends ScalaModule with MockitoSugar {
  /**
   * Bind the fake implementations the traits
   */
  def configure() {
    Logger.debug("Guice is loading TestModule")

//    bind[DateService].to[FakeDateServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))
  }

}
