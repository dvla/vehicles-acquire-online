package controllers

import Common.PrototypeHtml
import composition.WithApplication
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
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Second, Span}
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
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddressNotFound
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddressNotFound
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.selectedAddress

class BusinessChooseYourAddressUnitSpec extends UnitSpec {

  "present" should {
    "display the page if dealer details cached" in new WithApplication {
      whenReady(present, Timeout(Span(1, Second))) { r =>
        r.header.status should equal(OK)
      }
    }

    "display selected field when cookie exists" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddressUseUprn())
      val result = businessChooseYourAddressController(uprnFound = true).present(request)
      val content = contentAsString(result)
      content should include(TraderBusinessNameValid)
      content should include( s"""<option value="$selectedAddress" selected>""")
    }

    "display expected drop-down values" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController().submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$selectedAddress" >""")
    }

    "display unselected field when cookie does not exist" in new WithApplication {
      val content = contentAsString(present)
      content should include(TraderBusinessNameValid)
      content should not include "selected"
    }

    "redirect to setupTradeDetails page when present with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddressController(uprnFound = true).present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(
        isPrototypeBannerVisible = false
      ).present(request)
      contentAsString(result) should not include PrototypeHtml
    }

    "fetch the addresses for the trader's postcode from the address lookup micro service" in new WithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks(ordnanceSurveyUseUprn = true)
      val result = controller.present(request)
      whenReady(result) { r =>
        verify(addressServiceMock, times(1)).callPostcodeWebService(anyString(), any[TrackingId])(any[Lang])
      }
    }
  }

  "submit" should {
    "redirect to VehicleLookup page after a valid submit" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(uprnFound = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "return a bad request if no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController(uprnFound = true).submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "display expected drop-down values when no address selected" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = businessChooseYourAddressController().submit(request)
      val content = contentAsString(result)
      content should include( s"""<option value="$selectedAddress" >""")
    }

    "redirect to setupTradeDetails page when valid submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = businessChooseYourAddressController(uprnFound = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to setupTradeDetails page when bad submit with no dealer name cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
      val result = businessChooseYourAddressController(uprnFound = true).submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "write cookies and remove enter address manually cookie when UPRN found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = businessChooseYourAddressController(uprnFound = true).submit(request)
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

    "still call the micro service to fetch back addresses even though an invalid submission is made" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(addressSelected = "")
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks(ordnanceSurveyUseUprn = true)
      val result = controller.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
        verify(addressServiceMock, times(1)).callPostcodeWebService(anyString(), any[TrackingId])(any[Lang])
      }
    }

    "call the micro service to lookup the address by UPRN when a valid submission is made" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val (controller, addressServiceMock) = businessChooseYourAddressControllerAndMocks(uprnFound = true, ordnanceSurveyUseUprn = true)
      val result = controller.submit(request)
      whenReady(result) { r =>
        verify(addressServiceMock, times(1)).callPostcodeWebService(anyString(), any[TrackingId])(any[Lang])
      }
    }
  }

  private def buildCorrectlyPopulatedRequest(addressSelected: String = selectedAddress) = {
    FakeRequest().withFormUrlEncodedBody(
      AddressSelectId -> addressSelected)
  }

  private def businessChooseYourAddressController(uprnFound: Boolean = true,
                                        isPrototypeBannerVisible: Boolean = true) = {
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
    when(config.googleAnalyticsTrackingId).thenReturn(None) // Stub this config value.
    when(config.assetsUrl).thenReturn(None) // Stub this config value.

    new BusinessChooseYourAddress(addressLookupService)
  }

  private def businessChooseYourAddressControllerAndMocks(uprnFound: Boolean = true,
                                        isPrototypeBannerVisible: Boolean = true,
                                        ordnanceSurveyUseUprn: Boolean): (BusinessChooseYourAddress, AddressLookupWebService) = {
    val responsePostcode = if (uprnFound) responseValidForPostcodeToAddress else responseValidForPostcodeToAddressNotFound

    val addressLookupWebServiceMock = mock[AddressLookupWebService]
    when(addressLookupWebServiceMock.callPostcodeWebService(anyString(), any[TrackingId])(any[Lang]))
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
    businessChooseYourAddressController(uprnFound = true).present(request)
  }
}
