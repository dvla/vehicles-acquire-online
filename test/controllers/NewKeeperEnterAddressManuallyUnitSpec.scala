package controllers

import Common.PrototypeHtml
import helpers.acquire.CookieFactoryForUnitSpecs
import helpers.{TestWithApplication, UnitSpec}
import models.AcquireCacheKeyPrefix.CookiePrefix
import models.EnterAddressManuallyFormModel.Form.AddressAndPostcodeId
import org.mockito.Mockito.when
import pages.acquire.{SetupTradeDetailsPage, VehicleLookupPage, VehicleTaxOrSornPage}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, contentAsString, defaultAwaitTimeout}

import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.NewKeeperDetailsViewModel
import common.model.NewKeeperDetailsViewModel.newKeeperDetailsCacheKey
import common.testhelpers.CookieHelper.fetchCookiesFromHeaders
import common.testhelpers.JsonUtils.deserializeJsonToModel
import common.views.helpers.FormExtensions
import common.views.models.AddressLinesViewModel.Form.AddressLinesId
import common.views.models.AddressLinesViewModel.Form.BuildingNameOrNumberId
import common.views.models.AddressLinesViewModel.Form.Line2Id
import common.views.models.AddressLinesViewModel.Form.Line3Id
import common.views.models.AddressLinesViewModel.Form.PostTownId
import utils.helpers.Config
import views.acquire.EnterAddressManually.PostcodeId
import webserviceclients.fakes.FakeAddressLookupService.BuildingNameOrNumberValid
import webserviceclients.fakes.FakeAddressLookupService.Line2Valid
import webserviceclients.fakes.FakeAddressLookupService.Line3Valid
import webserviceclients.fakes.FakeAddressLookupService.PostTownValid
import SetupTradeDetailsPage.PostcodeValid

class NewKeeperEnterAddressManuallyUnitSpec extends UnitSpec {
  "present" should {
    "display the page when new business keeper has been chosen" in new TestWithApplication {
      whenReady(presentWithBusinessNewKeeper) { r =>
        r.header.status should equal(OK)
      }
    }

    "display the page when new private keeper has been chosen" in new TestWithApplication {
      whenReady(presentWithPrivateNewKeeper) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to VehicleLookup page when present is called but no new keeper is cached" in new TestWithApplication {
      val request = FakeRequest()
      val result = newKeeperEnterAddressManually.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to VehicleLookup page when present is called and both keeper types are cached in error" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
      val result = newKeeperEnterAddressManually.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "display populated fields when cookie exists" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.newKeeperEnterAddressManually())
      val result = newKeeperEnterAddressManually.present(request)
      val content = contentAsString(result)
      content should include(filledValue(BuildingNameOrNumberValid))
      content should include(filledValue(Line2Valid))
      content should include(filledValue(Line3Valid))
      content should include(filledValue(PostTownValid))
    }

    "display empty fields when cookie does not exist" in new TestWithApplication {
      val content = contentAsString(presentWithBusinessNewKeeper)
      content should not include filledValue(BuildingNameOrNumberValid)
      content should not include filledValue(Line2Valid)
      content should not include filledValue(Line3Valid)
      content should not include filledValue(PostTownValid)
    }

    "display prototype message when config set to true" in new TestWithApplication {
      contentAsString(presentWithBusinessNewKeeper) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new TestWithApplication {
      val request = FakeRequest()
      implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
      implicit val config: Config = mock[Config]
      when(config.isPrototypeBannerVisible).thenReturn(false) // Stub this config value.
      val enterAddressManuallyPrototypeNotVisible = new NewKeeperEnterAddressManually()

      val result = enterAddressManuallyPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to vehicle tax or sorn after a valid submission of mandatory fields" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid)
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
      val result = newKeeperEnterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleTaxOrSornPage.address))
      }
    }

    "redirect to vehicle tax or sorn after a valid submission of all fields" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> Line2Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> Line3Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid)
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
      val result = newKeeperEnterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleTaxOrSornPage.address))
      }
    }

    "return bad request when no data is entered" in new TestWithApplication {
      val request = FakeRequest()
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())

      val result = newKeeperEnterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "return bad request a valid postcode is entered without an address" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid)
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperEnterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "return bad request if no postcode is entered" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> Line2Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> Line3Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid)
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperEnterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "return bad request if an invalid postcode is entered" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> Line2Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> Line3Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> "SA1 1")
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperEnterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "remove commas and full stops from the end of each address line" in new TestWithApplication {
      val result = newKeeperEnterAddressManually.submit(requestWithValidDefaults(
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
      val result = newKeeperEnterAddressManually.submit(requestWithValidDefaults(
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
      val result = newKeeperEnterAddressManually.submit(requestWithValidDefaults(
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
      val result = newKeeperEnterAddressManually.submit(requestWithValidDefaults(
        buildingName = "m        "  // This should be a min length of 4 chars
      ))
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "not accept an address containing only full stops" in new TestWithApplication {
      val result = newKeeperEnterAddressManually.submit(requestWithValidDefaults(
        buildingName = "...")
      )

      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "redirect to vehicle lookup page when valid submit with no new keeper cached" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line2Id" -> Line2Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$Line3Id" -> Line3Valid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid)
      val result = newKeeperEnterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to vehicle lookup page when bad submit with no new keeper cached" in new TestWithApplication {
      val request = FakeRequest()
      val result = newKeeperEnterAddressManually.submit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "write cookie after a valid submission of all fields" in new TestWithApplication {
      val request = requestWithValidDefaults()
      val result = newKeeperEnterAddressManually.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(newKeeperDetailsCacheKey)
      }
    }

    "collapse error messages for buildingNameOrNumber" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> "",
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> PostTownValid,
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid)
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperEnterAddressManually.submit(request)
      val content = contentAsString(result)
      content should include("Building/number and street must contain between 4 and 30 characters")
    }

    "collapse error messages for post town" in new TestWithApplication {
      val request = FakeRequest().withFormUrlEncodedBody(
        s"$AddressAndPostcodeId.$AddressLinesId.$BuildingNameOrNumberId" -> BuildingNameOrNumberValid,
        s"$AddressAndPostcodeId.$AddressLinesId.$PostTownId" -> "",
        s"$AddressAndPostcodeId.$PostcodeId" -> PostcodeValid)
        .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
        .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
      val result = newKeeperEnterAddressManually.submit(request)
      val content = contentAsString(result)
      content should include("Post town requires a minimum length of three characters")
    }
  }
  private lazy val newKeeperEnterAddressManually = {
    injector.getInstance(classOf[NewKeeperEnterAddressManually])
  }

  private def validateAddressCookieValues(result: Future[Result], buildingName: String, line2: String,
                                          line3: String, postTown: String, postCode: String = PostcodeValid) = {

    whenReady(result) { r =>
      val cookies = fetchCookiesFromHeaders(r)
      cookies.find(_.name == newKeeperDetailsCacheKey) match {
        case Some(cookie) =>
          val json = cookie.value
          val model = deserializeJsonToModel[NewKeeperDetailsViewModel](json)
          val expectedData = Seq(buildingName,
            line2,
            line3,
            postTown,
            postCode)
          expectedData should equal(model.address.address)
        case None => fail(s"$newKeeperDetailsCacheKey cookie not found")
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
      s"$AddressAndPostcodeId.$PostcodeId" -> postCode)
      .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())

  private lazy val presentWithPrivateNewKeeper = {
    val request = FakeRequest()
      .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      .withCookies(CookieFactoryForUnitSpecs.privateKeeperDetailsModel())
    newKeeperEnterAddressManually.present(request)
  }

  private lazy val presentWithBusinessNewKeeper = {
    val request = FakeRequest()
      .withCookies(CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel())
      .withCookies(CookieFactoryForUnitSpecs.businessKeeperDetailsModel())
    newKeeperEnterAddressManually.present(request)
  }

  private def filledValue(value: String) =
    s"""value="$value""""
}
