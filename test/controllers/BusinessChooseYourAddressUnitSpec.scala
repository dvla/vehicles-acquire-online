package controllers

import Common.PrototypeHtml
import helpers.TestWithApplication
import helpers.UnitSpec
import helpers.acquire.CookieFactoryForUnitSpecs
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.BusinessChooseYourAddressFormModel.Form.AddressSelectId
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.{times, verify, when}
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import pages.acquire.SetupTradeDetailsPage.TraderBusinessNameValid
import pages.acquire.{SetupTradeDetailsPage, VehicleLookupPage}
import play.api.i18n.Lang
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, contentAsString, defaultAwaitTimeout}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{TrackingId, ClientSideSessionFactory}
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel.traderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.services.DateServiceImpl
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.verifyCookieHasBeenDiscarded
import uk.gov.dvla.vehicles.presentation.common.testhelpers.CookieHelper.verifyCookieHasNotBeenDiscarded
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.AddressLookupWebService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.selectedAddress

class BusinessChooseYourAddressUnitSpec extends UnitSpec {

  "present" should {
    "display the page if dealer details cached" in new TestWithApplication {
      whenReady(present, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display selected field when cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddressUseAddress())
      val result = businessChooseYourAddressController().present(request)
      val content = contentAsString(result)
      content should include(TraderBusinessNameValid)
      content should include( s"""<option value="$selectedAddress" selected>""")
    }

    "display expected drop-down values" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController().submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$selectedAddress" >""")
    }

    "display unselected field when cookie does not exist" in new TestWithApplication {
      val content = contentAsString(present)
      content should include(TraderBusinessNameValid)
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no dealer name cached" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddressController().present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new TestWithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(
        isPrototypeBannerVisible = false
      ).present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "fetch the addresses for the trader's postcode from the address lookup micro service" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks()
      val result = controller.present(request)
      whenReady(result) { r =>
        verify(addressServiceMock, times(1)).callAddresses(anyString(), any[TrackingId])(any[Lang])
      }
    }
  }

  "submit" should {
    "redirect to VehicleLookup page after a valid submit" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController().submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "return a bad request if no address selected" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController().submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController().submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$selectedAddress" >""")
    }

    "redirect to setupTradeDetails page when valid submit with no dealer name cached" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddressController().submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to setupTradeDetails page when bad submit with no dealer name cached" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      val result = businessChooseYourAddressController().submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "write cookies and remove enter address manually cookie when UPRN found" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = businessChooseYourAddressController().submit(request)
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

    "still call the micro service to fetch back addresses even though an invalid submission is made" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks()
      val result = controller.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
        verify(addressServiceMock, times(1)).callAddresses(anyString(), any[TrackingId])(any[Lang])
      }
    }

    "call the micro service to lookup the address by UPRN when a valid submission is made" in new TestWithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks()
      val result = controller.submit(request)
      whenReady(result) { r =>
        verify(addressServiceMock, times(1)).callAddresses(anyString(), any[TrackingId])(any[Lang])
      }
    }
  }

  private def buildCorrectlyPopulatedRequest(addressSelected: String = selectedAddress) = {
    FakeRequest().withFormUrlEncodedBody(
      AddressSelectId -> addressSelected)
  }

  private def businessChooseYourAddressController(isPrototypeBannerVisible: Boolean = true) = {
    val responsePostcode = responseValidForPostcodeToAddress
    val fakeWebService = new FakeAddressLookupWebServiceImpl(responsePostcode)
    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] = invocation.getArguments()(1).asInstanceOf[Future[_]]
    })
    val addressLookupService = new AddressLookupServiceImpl(fakeWebService, new DateServiceImpl, healthStatsMock)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
    when(config.assetsUrl).thenReturn(None) // Stub this config value.

    new BusinessChooseYourAddress(addressLookupService)
  }

  private def businessChooseYourAddressControllerAndMocks(isPrototypeBannerVisible: Boolean = true): (BusinessChooseYourAddress, AddressLookupWebService) = {
    val responsePostcode = responseValidForPostcodeToAddress

    val addressLookupWebServiceMock = mock[AddressLookupWebService]
    when(addressLookupWebServiceMock.callAddresses(anyString(), any[TrackingId])(any[Lang]))
      .thenReturn(responsePostcode)

    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] = invocation.getArguments()(1).asInstanceOf[Future[_]]
    })

    val addressLookupService = new AddressLookupServiceImpl(addressLookupWebServiceMock, new DateServiceImpl, healthStatsMock)

    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
    when(config.assetsUrl).thenReturn(None) // Stub this config value.

    (new BusinessChooseYourAddress(addressLookupService), addressLookupWebServiceMock)
  }

  private def present = {
    val request = FakeRequest().withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
    businessChooseYourAddressController().present(request)
  }
}
