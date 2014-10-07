package controllers.acquire

import controllers.acquire.Common.PrototypeHtml
import controllers.NewKeeperChooseYourAddress
import helpers.common.CookieHelper
import CookieHelper.fetchCookiesFromHeaders
import helpers.UnitSpec
import org.mockito.Mockito.when
import pages.acquire.CompleteAndConfirmPage
import helpers.acquire.CookieFactoryForUnitSpecs
import pages.acquire.VehicleLookupPage
import pages.common.UprnNotFoundPage
import pages.acquire.PrivateKeeperDetailsPage.{FirstNameValid, LastNameValid}
import pages.acquire.BusinessKeeperDetailsPage.BusinessNameValid
import models.NewKeeperChooseYourAddressFormModel.NewKeeperChooseYourAddressCacheKey
import models.NewKeeperChooseYourAddressFormModel.Form.AddressSelectId
import models.NewKeeperDetailsViewModel.NewKeeperDetailsCacheKey
import play.api.mvc.Cookies
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, LOCATION, BAD_REQUEST, SET_COOKIE, contentAsString, defaultAwaitTimeout}
import play.api.test.WithApplication
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.TraderDetailsModel
import TraderDetailsModel.TraderDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl
import utils.helpers.Config
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddressNotFound
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddressNotFound
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.UprnValid

final class NewKeeperChooseYourAddressUnitSpec extends UnitSpec {
  "present" should {
    "display the page if private new keeper details cached" in new WithApplication {
      whenReady(presentWithPrivateNewKeeper, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display the page if business new keeper details cached" in new WithApplication {
      whenReady(presentWithBusinessNewKeeper, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "display selected field when private new keeper cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperChooseYourAddress())
      val result = newKeeperChooseYourAddressWithUprnFound.present(request)
      val content = contentAsString(result)
      content should include(FirstNameValid)
      content should include(LastNameValid)
      content should include( s"""<option value="$UprnValid" selected>""")
    }

    "display selected field when business new keeper cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel()).
        withCookies(CookieFactoryForUnitSpecs.newKeeperChooseYourAddress())
      val result = newKeeperChooseYourAddressWithUprnFound.present(request)
      val content = contentAsString(result)
      content should include(BusinessNameValid)
      content should include( s"""<option value="$UprnValid" selected>""")
    }

    "display unselected field when cookie does not exist for private new keeper" in new WithApplication {
      val content = contentAsString(presentWithPrivateNewKeeper)
      content should include(FirstNameValid)
      content should include(LastNameValid)
      content should not include "selected"
    }

    "display unselected field when cookie does not exist for business new keeper" in new WithApplication {
      val content = contentAsString(presentWithBusinessNewKeeper)
      content should include(BusinessNameValid)
      content should not include "selected"
    }

    "redirect to vehicle lookup page when present is called with no keeper details cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = newKeeperChooseYourAddressWithUprnFound.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "display prototype message when config set to true for new private keeper" in new WithApplication {
      contentAsString(presentWithPrivateNewKeeper) should include(PrototypeHtml)
    }

    "display prototype message when config set to true for new business keeper" in new WithApplication {
      contentAsString(presentWithBusinessNewKeeper) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest().
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperChooseYourAddressWithFakeWebService(isPrototypeBannerVisible = false).present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to complete and confirm page after a valid submit for private keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperChooseYourAddressWithUprnFound.submit(request)
      whenReady(result) { r =>
          r.header.headers.get(LOCATION) should equal(Some(CompleteAndConfirmPage.address))
      }
    }

    "redirect to complete and confirm page after a valid submit for business keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
      val result = newKeeperChooseYourAddressWithUprnFound.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(CompleteAndConfirmPage.address))
      }
    }

    "return a bad request if not address selected for private keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(newKeeperUprn = "").
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperChooseYourAddressWithUprnFound.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "return a bad request if not address selected for business keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(newKeeperUprn = "").
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
      val result = newKeeperChooseYourAddressWithUprnFound.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to vehicle lookup page when valid submit with keeper details cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = newKeeperChooseYourAddressWithUprnFound.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to vehicle lookup page when bad submit with no keeper details cached" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(newKeeperUprn = "")
      val result = newKeeperChooseYourAddressWithUprnFound.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice using new private keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperChooseYourAddressWithUprnNotFound.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "redirect to UprnNotFound page when submit with but uprn not found by the webservice using new business keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
      val result = newKeeperChooseYourAddressWithUprnNotFound.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(UprnNotFoundPage.address))
      }
    }

    "write cookie when uprn found for private keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperChooseYourAddressWithUprnFound.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(NewKeeperChooseYourAddressCacheKey, NewKeeperDetailsCacheKey)
      }
    }

    "write cookie when uprn found for business keeper" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
      val result = newKeeperChooseYourAddressWithUprnFound.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(NewKeeperChooseYourAddressCacheKey, NewKeeperDetailsCacheKey)
      }
    }

    "does not write cookie when uprn not found" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = newKeeperChooseYourAddressWithUprnNotFound.submit(request)
      whenReady(result) { r =>
        val cookies = r.header.headers.get(SET_COOKIE).toSeq.flatMap(Cookies.decode)
        cookies.map(_.name) should contain noneOf(NewKeeperChooseYourAddressCacheKey, TraderDetailsCacheKey)
      }
    }
  }

  private def newKeeperChooseYourAddressWithFakeWebService(uprnFound: Boolean = true,
                                                          isPrototypeBannerVisible: Boolean = true) = {
    val responsePostcode = if (uprnFound) responseValidForPostcodeToAddress else responseValidForPostcodeToAddressNotFound
    val responseUprn = if (uprnFound) responseValidForUprnToAddress else responseValidForUprnToAddressNotFound
    val fakeWebService = new FakeAddressLookupWebServiceImpl(responsePostcode, responseUprn)
    val addressLookupService = new AddressLookupServiceImpl(fakeWebService)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    when(config.isPrototypeBannerVisible).thenReturn(isPrototypeBannerVisible) // Stub this config value.
    new NewKeeperChooseYourAddress(addressLookupService)
  }

  private def buildCorrectlyPopulatedRequest(newKeeperUprn: String = UprnValid.toString) = {
    FakeRequest().withFormUrlEncodedBody(
      AddressSelectId -> newKeeperUprn)
  }

  private val newKeeperChooseYourAddressWithUprnFound =
    newKeeperChooseYourAddressWithFakeWebService()

  private val newKeeperChooseYourAddressWithUprnNotFound = newKeeperChooseYourAddressWithFakeWebService(uprnFound = false)

  private lazy val presentWithPrivateNewKeeper = {
    val request = FakeRequest().withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
    newKeeperChooseYourAddressWithUprnFound.present(request)
  }

  private lazy val presentWithBusinessNewKeeper = {
    val request = FakeRequest().withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
    newKeeperChooseYourAddressWithUprnFound.present(request)
  }
}
