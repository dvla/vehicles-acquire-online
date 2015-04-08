package controllers.acquire

import controllers.BusinessChooseYourAddress
import controllers.acquire.Common.PrototypeHtml
import helpers.acquire.CookieFactoryForUnitSpecs
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.common.CookieHelper.verifyCookieHasBeenDiscarded
import helpers.common.CookieHelper.verifyCookieHasNotBeenDiscarded
import helpers.UnitSpec
import helpers.WithApplication
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import pages.acquire.{SetupTradeDetailsPage, VehicleLookupPage}
import pages.acquire.SetupTradeDetailsPage.TraderBusinessNameValid
import pages.common.UprnNotFoundPage
import play.api.i18n.Lang
import play.api.mvc.Cookies
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, contentAsString, defaultAwaitTimeout, LOCATION, OK, SET_COOKIE}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.traderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.services.DateServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupWebService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddressNotFound
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddressNotFound
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.UprnValid

class BusinessChooseYourAddressUnitSpec extends UnitSpec {
  final val addressIndex = "0"

  "present (with UPRN enabled)" should {
    "display the page if dealer details cached" in new WithApplication {
      whenReady(present(ordnanceSurveyUseUprn = true), timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddressUseUprn())
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = true).present(request)
      val content = contentAsString(result)
      content should include(TraderBusinessNameValid)
      content should include( s"""<option value="$UprnValid" selected>""")
    }

    "display expected drop-down values" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(ordnanceSurveyUseUprn = true).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$UprnValid" >""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present(ordnanceSurveyUseUprn = true))
      content should include(TraderBusinessNameValid)
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = true).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present(ordnanceSurveyUseUprn = true)) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(
        isPrototypeBannerVisible = false,
        ordnanceSurveyUseUprn = true
      ).present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "fetch the addresses for the trader's postcode from the address lookup micro service" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks(ordnanceSurveyUseUprn = true)
      val result = controller.present(request)
      whenReady(result) { r =>
        verify(addressServiceMock, times(1)).callPostcodeWebService(anyString(), anyString(), any[Option[Boolean]])(any[Lang])
      }
    }
  }

  "present (with UPRN not enabled)" should {
    "display the page if dealer details cached" in new WithApplication {
      whenReady(present(ordnanceSurveyUseUprn = false), timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress())
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = false).present(request)
      val content = contentAsString(result)
      content should include(TraderBusinessNameValid)
      content should include( s"""<option value="0" selected>""")
    }

    "display expected drop-down values" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(ordnanceSurveyUseUprn = false).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" >""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present(ordnanceSurveyUseUprn = false))
      content should include(TraderBusinessNameValid)
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = false).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present(ordnanceSurveyUseUprn = false)) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(
        isPrototypeBannerVisible = false,
        ordnanceSurveyUseUprn = false
      ).present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "fetch the addresses for the trader's postcode from the address lookup micro service" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks(ordnanceSurveyUseUprn = false)
      val result = controller.present(request)
      whenReady(result) { r =>
        verify(addressServiceMock, times(1)).callPostcodeWebService(anyString(), anyString(), any[Option[Boolean]])(any[Lang])
      }
    }
  }

  "submit (with UPRN enabled)" should {
    "redirect to VehicleLookup page after a valid submit" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "return a bad request if no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(ordnanceSurveyUseUprn = true).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$UprnValid" >""")
    }

    "redirect to setupTradeDetails page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to setupTradeDetails page when bad submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to UprnNotFound page when submit but uprn not found by the webservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "0").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(uprnFound = false, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookies and remove enter address manually cookie when UPRN found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = true).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          BusinessChooseYourAddressCacheKey,
          EnterAddressManuallyCacheKey,
          traderDetailsCacheKey)
        verifyCookieHasBeenDiscarded(EnterAddressManuallyCacheKey, cookies)
        verifyCookieHasNotBeenDiscarded(BusinessChooseYourAddressCacheKey, cookies)
        verifyCookieHasNotBeenDiscarded(traderDetailsCacheKey, cookies)
      }
    }

    "not call the micro service to lookup the address by UPRN when an invalid submission is made" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks(uprnFound = true, ordnanceSurveyUseUprn = true)
      val result = controller.submit(request)
      whenReady(result, timeout) { r =>
        r.header.status should equal(BAD_REQUEST)
        verify(addressServiceMock, never()).callUprnWebService(anyString(), anyString())(any[Lang])
      }
    }

    "call the micro service to lookup the address by UPRN when a valid submission is made" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks(uprnFound = true, ordnanceSurveyUseUprn = true)
      val result = controller.submit(request)
      whenReady(result) { r =>
        verify(addressServiceMock, times(1)).callUprnWebService(anyString(), anyString())(any[Lang])
      }
    }
  }

  "submit (with UPRN not enabled)" should {
    "redirect to VehicleLookup page after a valid submit" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = addressIndex).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "return a bad request if no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(ordnanceSurveyUseUprn = false).submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="0" >""")
    }

    "redirect to setupTradeDetails page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = addressIndex)
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to setupTradeDetails page when bad submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = addressIndex).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(uprnFound = false, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookies and remove enter address manually cookie when uprn found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = addressIndex).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          BusinessChooseYourAddressCacheKey,
          EnterAddressManuallyCacheKey,
          traderDetailsCacheKey)
        verifyCookieHasBeenDiscarded(EnterAddressManuallyCacheKey, cookies)
        verifyCookieHasNotBeenDiscarded(BusinessChooseYourAddressCacheKey, cookies)
        verifyCookieHasNotBeenDiscarded(traderDetailsCacheKey, cookies)
      }
    }

    "does not write cookie when uprn not found" in new WithApplication {
        val request = buildCorrectlyPopulatedRequest(addressSelected = addressIndex).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(uprnFound = false, ordnanceSurveyUseUprn = false).submit(request)
      whenReady(result) { r =>
        val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
        cookies.map(_.name) should contain noneOf(BusinessChooseYourAddressCacheKey, traderDetailsCacheKey)
      }
    }

    "still call the micro service to fetch back addresses even though an invalid submission is made" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "").
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks(uprnFound = true, ordnanceSurveyUseUprn = false)
      val result = controller.submit(request)
      whenReady(result, timeout) { r =>
        r.header.status should equal(BAD_REQUEST)
        verify(addressServiceMock, times(1)).callPostcodeWebService(anyString(), anyString(), any[Option[Boolean]])(any[Lang])
      }
    }

    "call the micro service to lookup the address by postcode when a valid submission is made" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks(uprnFound = true, ordnanceSurveyUseUprn = false)
      val result = controller.submit(request)
      whenReady(result) { r =>
        verify(addressServiceMock, times(1)).callPostcodeWebService(anyString(), anyString(), any[Option[Boolean]])(any[Lang])
      }
    }
  }

  private def buildCorrectlyPopulatedRequest(addressSelected: String = UprnValid.toString) = {
    FakeRequest().withFormUrlEncodedBody(
      AddressSelectId -> addressSelected)
  }

  private def businessChooseYourAddressController(uprnFound: Boolean = true,
                                        isPrototypeBannerVisible: Boolean = true,
                                        ordnanceSurveyUseUprn: Boolean) = {
    val responsePostcode = if (uprnFound) responseValidForPostcodeToAddress else responseValidForPostcodeToAddressNotFound
    val responseUprn = if (uprnFound) responseValidForUprnToAddress else responseValidForUprnToAddressNotFound
    val fakeWebService = new FakeAddressLookupWebServiceImpl(responsePostcode, responseUprn)
    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] = invocation.getArguments()(1).asInstanceOf[Future[_]]
    })
    val addressLookupService = new AddressLookupServiceImpl(fakeWebService, new DateServiceImpl, healthStatsMock)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.ordnanceSurveyUseUprn).thenReturn(ordnanceSurveyUseUprn) // Stub this config value.
    when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
    when(config.assetsUrl).thenReturn(None) // Stub this config value.

    new BusinessChooseYourAddress(addressLookupService)
  }

  private def businessChooseYourAddressControllerAndMocks(uprnFound: Boolean = true,
                                        isPrototypeBannerVisible: Boolean = true,
                                        ordnanceSurveyUseUprn: Boolean): (BusinessChooseYourAddress, AddressLookupWebService) = {
    val responsePostcode = if (uprnFound) responseValidForPostcodeToAddress else responseValidForPostcodeToAddressNotFound
    val responseUprn = if (uprnFound) responseValidForUprnToAddress else responseValidForUprnToAddressNotFound

    val addressLookupWebServiceMock = mock[AddressLookupWebService]
    when(addressLookupWebServiceMock.callPostcodeWebService(anyString(), anyString(), any[Option[Boolean]])(any[Lang])).
      thenReturn(responsePostcode)
    when(addressLookupWebServiceMock.callUprnWebService(anyString(), anyString())(any[Lang])).
      thenReturn(responseUprn)

    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] = invocation.getArguments()(1).asInstanceOf[Future[_]]
    })

    val addressLookupService = new AddressLookupServiceImpl(addressLookupWebServiceMock, new DateServiceImpl, healthStatsMock)

    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.ordnanceSurveyUseUprn).thenReturn(ordnanceSurveyUseUprn) // Stub this config value.
    when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
    when(config.assetsUrl).thenReturn(None) // Stub this config value.

    (new BusinessChooseYourAddress(addressLookupService), addressLookupWebServiceMock)
  }

  private def present(ordnanceSurveyUseUprn: Boolean) = {
    val request = FakeRequest().withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
    businessChooseYourAddressController(uprnFound = true, ordnanceSurveyUseUprn = ordnanceSurveyUseUprn).present(request)
  }
}
