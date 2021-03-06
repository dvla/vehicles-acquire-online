package controllers

import Common.PrototypeHtml
import helpers.acquire.CookieFactoryForUnitSpecs
import helpers.{UnitSpec, TestWithApplication}
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.BusinessChooseYourAddressFormModel.BusinessChooseYourAddressCacheKey
import models.EnterAddressManuallyFormModel.Form.AddressAndPostcodeId
import models.EnterAddressManuallyFormModel.EnterAddressManuallyCacheKey
import org.mockito.Mockito.when
import pages.acquire.{VehicleLookupPage, SetupTradeDetailsPage}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, contentAsString, defaultAwaitTimeout, LOCATION, OK}
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.TraderDetailsModel
import common.model.TraderDetailsModel.traderDetailsCacheKey
import common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import common.testhelpers.CookieHelper.verifyCookieHasBeenDiscarded
import common.testhelpers.CookieHelper.verifyCookieHasNotBeenDiscarded
import common.testhelpers.JsonUtils.deserializeJsonToModel
import common.views.helpers.FormExtensions
import common.views.models.AddressLinesViewModel.Form.{AddressLinesId, BuildingNameOrNumberId, Line2Id, Line3Id, PostTownId}
import utils.helpers.Config
import views.acquire.EnterAddressManually.PostcodeId
import webserviceclients.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import webserviceclients.fakes.FakeAddressLookupService.Line2Valid
import webserviceclients.fakes.FakeAddressLookupService.Line3Valid
import webserviceclients.fakes.FakeAddressLookupService.PostTownValid
import SetupTradeDetailsPage.PostcodeValid

class EnterAddressManuallyUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new TestWithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to SetupTraderDetails page when present with no dealer name cached" in new TestWithApplication {
      val request = FakeRequest()
      val result = enterAddressManually.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "display populated fields when cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
        .withCookies(CookieFactoryForUnitSpecs.enterAddressManually())
      val result = enterAddressManually.present(request)
      val content = contentAsString(result)
      content should include(filledValue(BuildingNameOrNumberValid))
      content should include(filledValue(Line2Valid))
      content should include(filledValue(Line3Valid))
      content should include(filledValue(PostTownValid))
    }

    "display empty fields when cookie does not exist" in new TestWithApplication {
      val content = contentAsString(present)
      content should not include filledValue(BuildingNameOrNumberValid)
      content should not include filledValue(Line2Valid)
      content should not include filledValue(Line3Valid)
      content should not include filledValue(PostTownValid)
    }

    "display prototype message when config set to true" in new TestWithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      val enterAddressManuallyPrototypeNotVisible = new EnterAddressManually()

      val result = enterAddressManuallyPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to vehicle lookup after a valid submission of mandatory fields" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to vehicle lookup after a valid submission of all fields" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> Line2Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> Line3Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "write cookies and remove enter address business choose your address cookie when uprn found" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails()).
        withCookies(CookieFactoryForUnitSpecs.businessChooseYourAddress())
      val result = enterAddressManually.submit(request)

      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain allOf(
          BusinessChooseYourAddressCacheKey,
          EnterAddressManuallyCacheKey,
          traderDetailsCacheKey
          )
        verifyCookieHasBeenDiscarded(BusinessChooseYourAddressCacheKey, cookies)
        verifyCookieHasNotBeenDiscarded(EnterAddressManuallyCacheKey, cookies)
        verifyCookieHasNotBeenDiscarded(traderDetailsCacheKey, cookies)
      }
    }

    "return bad request when no data is entered" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody().
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())

      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "return bad request a valid postcode is entered without an address" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "remove commas and full stops from the end of each address line" in new TestWithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "my house,",
        line2 = "my street.",
        line3 = "my area.",
        postTown = "my town,"
      ))

      validateAddressCookieValues(result,
        buildingName = "MY HOUSE,",
        line2 = "MY STREET.",
        line3 = "MY AREA.",
        postTown = "MY TOWN,"
      )
    }

    "remove multiple commas and full stops from the end of each address line" in new TestWithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "my house,.,..,,",
        line2 = "my street...,,.,",
        line3 = "my area.,,..",
        postTown = "my town,,,.,,,."
      ))

      validateAddressCookieValues(result,
        buildingName = "MY HOUSE,.,..,,",
        line2 = "MY STREET...,,.,",
        line3 = "MY AREA.,,..",
        postTown = "MY TOWN,,,.,,,."
      )
    }

    "not remove multiple commas and full stops from the middle of address lines" in new TestWithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "my house 1.1",
        line2 = "st. something street",
        line3 = "st. johns",
        postTown = "my t.own"
      ))

      validateAddressCookieValues(result,
        buildingName = "MY HOUSE 1.1",
        line2 = "ST. SOMETHING STREET",
        line3 = "ST. JOHNS",
        postTown = "MY T.OWN"
      )
    }

    "remove commas, but still applies the min length rule" in new TestWithApplication {
      FormExtensions.trimNonWhiteListedChars("""[A-Za-z0-9\-]""")(",, m...,,,,   ") should equal("m")
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "m       "  // This should be a min length of 4 chars
      ))
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "not accept an address containing only full stops" in new TestWithApplication {
      val result = enterAddressManually.submit(requestWithValidDefaults(
        buildingName = "...")
      )

      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to SetupTraderDetails page when valid submit with no dealer name cached" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> Line2Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> Line3Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid)
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "redirect to SetupTradeDetails page when bad submit with no dealer name cached" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody()
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(SetupTradeDetailsPage.address))
      }
    }

    "write cookie after a valid submission of all fields" in new TestWithApplication {
      val request = requestWithValidDefaults()
      val result = enterAddressManually.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(traderDetailsCacheKey)
      }
    }

    "collapse error messages for buildingNameOrNumber" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> "",
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = enterAddressManually.submit(request)
      val content = contentAsString(result)
      content should include("Building/number and street must contain between 4 and 30 characters")
    }

    "collapse error messages for post town" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> "",
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid).
        withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
      val result = enterAddressManually.submit(request)
      val content = contentAsString(result)
      content should include("Post town requires a minimum length of three characters")
    }
  }

  private lazy val enterAddressManually = {
    injector.getInstance(classOf[EnterAddressManually])
  }

  private val traderDetailsCookieName = "acq-traderDetails"

  private def validateAddressCookieValues(result: Future[Result], buildingName: String, line2: String,
                                          line3: String, postTown: String, postCode: String = PostcodeValid) = {

    whenReady(result) { r =>
      val cookies = fetchCookiesFromHeaders(r)
      cookies.find(_.name == traderDetailsCookieName) match {
        case Some(cookie) =>
          val json = cookie.value
          val model = deserializeJsonToModel[TraderDetailsModel](json)
          val expectedData = Seq(buildingName,
            line2,
            line3,
            postTown,
            postCode)
          expectedData should equal(model.traderAddress.address)
        case None => fail(s"$traderDetailsCookieName cookie not found")
      }
    }
  }

  private def requestWithValidDefaults(buildingName: String = BuildingNameOrNumberValid,
                                       line2: String = Line2Valid,
                                       line3: String = Line3Valid,
                                       postTown: String = PostTownValid,
                                       postCode: String = PostcodeValid) =

    FakeRequest().withFormUrlEncodedBody(
      s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> buildingName,
      s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> line2,
      s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> line3,
      s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> postTown,
      s"$AddressAndPostcodeId.$PostcodeId" -> postCode).
      withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())

  private lazy val present = {
    val request = FakeRequest().
      withCookies(CookieFactoryForUnitSpecs.setupTradeDetails())
    enterAddressManually.present(request)
  }

  private def filledValue(value: String) =
    s"""value="$value""""
}
