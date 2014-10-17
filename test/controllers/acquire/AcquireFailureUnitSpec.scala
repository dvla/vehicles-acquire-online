package controllers.acquire

import controllers.AcquireFailure
import controllers.acquire.Common.PrototypeHtml
import helpers.{UnitSpec, WithApplication}
import helpers.acquire.CookieFactoryForUnitSpecs
import models.AcquireCompletionViewModel
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class AcquireFailureUnitSpec extends UnitSpec {
  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "not display progress bar" in new WithApplication {
      contentAsString(present) should not include "Step "
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      val AcquireFailurePrototypeNotVisible = new AcquireFailure()

      val result = AcquireFailurePrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private lazy val present = {
    val AcquireFailure = injector.getInstance(classOf[AcquireFailure])
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.acquireCompletionViewModel())
    AcquireFailure.present(request)
  }
}