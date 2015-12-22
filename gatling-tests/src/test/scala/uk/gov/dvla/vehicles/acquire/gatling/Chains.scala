package uk.gov.dvla.vehicles.acquire.gatling

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.http.Predef._
import Headers.{headers_accept_html, headers_accept_png, headers_x_www_form_urlencoded}

class Chains(data: RecordSeqFeederBuilder[String]) {

  private final val BeforeYouStartPageTitle = "Selling a vehicle out of trade"
  private final val SetupTradeDetailsPageTitle = "Provide trader details"
  private final val BusinessChooseYourAddressPageTitle = "Select trader address"
  private final val TraderPlayBackHeading = "Vehicle being sold from"
  private final val VehicleLookupPageTitle = "Enter vehicle details"
  private final val BusinessKeeperDetailsPageTitle = "Enter new keeper details"
  private final val PrivateKeeperDetailsPageTitle = "Enter new keeper details"
  private final val NewKeeperChooseYourAddressPageTitle = "Select new keeper address"
  private final val VehicleTaxOrSorn = "Vehicle tax or SORN"
  private final val CompleteAndConfirmPageTitle = "Complete and confirm"
  private final val VehicleDetailsPlaybackHeading = "Vehicle details"
  private final val KeeperDetailsPlaybackHeading = "New keeper details"
  private final val SummaryPageTitle = "Summary"
  private final val TransactionDetailsPlaybackHeading = "Transaction details"
  private final val VehicleLookupFailurePageTitle = "Unable to find a vehicle record"

  def verifyAssetsAreAccessible =
    exec(http("screen.min.css")
      .get(s"/assets/screen.min.css")
    )
      .exec(http("print.min.css")
      .get(s"/assets/print.min.css")
      )
      .exec(
        http("govuk-crest.png")
          .get(s"/assets/lib/vehicles-presentation-common/images/govuk-crest.png")
          .headers(headers_accept_png)
      )
      .exec(
        http("govuk-crest.png")
          .get(s"/assets/lib/vehicles-presentation-common/images/govuk-crest-2x.png")
          .headers(headers_accept_png)
      )
      .exec(
        http("govuk-crest.png")
          .get(s"/assets/lib/vehicles-presentation-common/images/gov.uk_logotype_crown.png")
          .headers(headers_accept_png)
      )
      .exec(
        http("govuk-crest.png")
          .get(s"/assets/lib/vehicles-presentation-common/images/open-government-licence_2x.png")
          .headers(headers_accept_png)
      )
      .exec(http("require.js")
      .get(s"/assets/javascripts/require.js")
      .headers(Map( """Accept""" -> """*/*"""))
      )
      .exec(http("custom.js")
      .get(s"/assets/javascripts/main.js")
      .headers(Map( """Accept""" -> """*/*"""))
      )

  def beforeYouStart = {
    val url = "/before-you-start"
    val chainTitle = s"GET $url"
    exitBlockOnFail(
      exec(
        http(chainTitle)
          .get(url)
          .headers(headers_accept_html)
          // Assertions
          .check(status.is(200))
          .check(regex(BeforeYouStartPageTitle).exists)
      )
    )
  }

  def traderDetailsPage = {
    val url = "/setup-trade-details"
    val chainTitle = s"GET $url"
    exitBlockOnFail(
      exec(
        http(chainTitle)
          .get(url)
          .headers(headers_accept_html)
          // Assertions
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          .check(regex(SetupTradeDetailsPageTitle).exists)
      )
    )
  }

  def traderDetailsSubmit = {
    val url = "/setup-trade-details"
    val chainTitle = s"POST $url"
    exitBlockOnFail(
      feed(data)
        .exec(
          http(chainTitle)
            .post(url)
            .headers(headers_accept_html)
            .formParam("traderName", "${traderName}")
            .formParam("traderPostcode", "${traderPostcode}")
            .formParam("traderEmailOption", "invisible")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            // Assertions
            .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
            .check(regex(BusinessChooseYourAddressPageTitle).exists)
        )
    )
  }

  def businessChooseYourAddress = {
    val url = "/business-choose-your-address"
    val chainTitle = s"POST $url"
    exitBlockOnFail(
      exec(
        http(chainTitle)
          .post(url)
          .headers(headers_x_www_form_urlencoded)
          .formParam("disposal_businessChooseYourAddress_addressSelect", "Not real street 1, Not real street2, Not real town, QQ9 9QQ")
          .check(regex(VehicleLookupPageTitle).exists)
          .formParam("csrf_prevention_token", "${csrf_prevention_token}")
          .formParam("action", "")
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
      )
    )
  }

  def vehicleLookup = {
    val url = "/vehicle-lookup"
    val chainTitle = s"GET $url"
    exitBlockOnFail(
      exec(
        http(chainTitle)
          .get(url)
          .headers(headers_accept_html)
          .check(status.is(200))
          // Assertions
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
//          .check(regex(TraderPlayBackHeading).exists)
          .check(regex(VehicleLookupPageTitle).exists)
          .check(regex("${expected_traderName}").exists)
      )
    )
  }

  def vehicleLookupSubmitNewBusinessKeeper = vehicleLookupSubmit(BusinessKeeperDetailsPageTitle)
  def vehicleLookupSubmitNewPrivateKeeper = vehicleLookupSubmit(PrivateKeeperDetailsPageTitle)

  private def vehicleLookupSubmit(expectedPageTitle: String) = {
    val url = "/vehicle-lookup"
    val chainTitle = s"POST $url"
    exitBlockOnFail(
      feed(data)
        .exec(
          http(chainTitle)
            .post(url)
            .headers(headers_x_www_form_urlencoded)
            .formParam("vehicleRegistrationNumber", "${vehicleRegistrationNumber}")
            .formParam("documentReferenceNumber", "${documentReferenceNumber}")
            .formParam("vehicleSoldTo", "${vehicleSoldTo}")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            // Assertions
            .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
            .check(regex(expectedPageTitle).exists)
            .check(regex(VehicleDetailsPlaybackHeading).exists)
            .check(regex("${expected_registrationNumberFormatted}").exists)
            .check(regex("${expected_make}").exists)
        )
    )
  }

  def businessKeeperDetailsSubmit = {
    val url = "/business-keeper-details"
    val chainTitle = s"POST $url"
    exitBlockOnFail(
      exec(
        http(chainTitle)
          .post(url)
          .headers(headers_x_www_form_urlencoded)
          .formParam("fleetNumber", "${fleetNumber}")
          .formParam("fleetNumberOption", "invisible")
          .formParam("businessName", "${businessName}")
          .formParam("businesskeeper_option_email", "visible")
          .formParam("businesskeeper_email.email", "${businessEmail}")
          .formParam("businesskeeper_email.email-verify", "${businessEmail}")
          .formParam("businesskeeper_postcode", "${businessPostcode}")
          .formParam("csrf_prevention_token", "${csrf_prevention_token}")
          .formParam("action", "")
          // Assertions
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          .check(regex(NewKeeperChooseYourAddressPageTitle).exists)
          .check(regex(VehicleDetailsPlaybackHeading).exists)
          .check(regex("${expected_registrationNumberFormatted}").exists)
          .check(regex("${expected_make}").exists)
          .check(regex("${expected_model}").exists)
      )
    )
  }

  def newKeeperChooseYourAddressSubmit = {
    val url = "/new-keeper-choose-your-address"
    val chainTitle = s"POST $url"
    exitBlockOnFail(
      exec(
        http(chainTitle)
          .post(url)
          .headers(headers_x_www_form_urlencoded)
          .formParam("newKeeperChooseYourAddress_addressSelect", "Not real street 1, Not real street2, Not real town, QQ9 9QQ")
          .formParam("csrf_prevention_token", "${csrf_prevention_token}")
          .formParam("action", "")
          // Assertions
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          .check(regex(VehicleTaxOrSorn).exists)
          .check(regex(KeeperDetailsPlaybackHeading).exists)
          .check(regex("${expected_buyerName}").exists)
          .check(regex("${expected_buyerAddressLine1}").exists)
          .check(regex("${expected_buyerAddressLine2}").exists)
          .check(regex("${expected_buyerAddressPostcode}").exists)
          .check(regex(VehicleDetailsPlaybackHeading).exists)
          .check(regex("${expected_registrationNumberFormatted}").exists)
          .check(regex("${expected_make}").exists)
          .check(regex("${expected_model}").exists)
      )
    )
  }

  def privateKeeperDetailsSubmit = {
    val url = "/private-keeper-details"
    val chainTitle = s"POST $url"
    exitBlockOnFail(
      exec(
        http(chainTitle)
          .post(url)
          .headers(headers_x_www_form_urlencoded)
          .formParam("privatekeeper_title.titleOption", "${privateKeeperTitle}")
          .formParam("privatekeeper_title.titleText", "${privateKeeperTitleText}")
          .formParam("privatekeeper_firstname", "${privateKeeperFirstName}")
          .formParam("privatekeeper_lastname", "${privateKeeperLastName}")
          .formParam("privatekeeper_dateofbirth.day", "${dateOfBirthDay}")
          .formParam("privatekeeper_dateofbirth.month", "${dateOfBirthMonth}")
          .formParam("privatekeeper_dateofbirth.year", "${dateOfBirthYear}")
          .formParam("privatekeeper_drivernumber", "${driverNumber}")
          .formParam("privatekeeper_option_email", "visible")
          .formParam("privatekeeper_email.email", "${email}")
          .formParam("privatekeeper_email.email-verify", "${email}")
          .formParam("privatekeeper_postcode", "${privateKeeperPostcode}")
          .formParam("csrf_prevention_token", "${csrf_prevention_token}")
          .formParam("action", "")
          // Assertions
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          .check(regex(NewKeeperChooseYourAddressPageTitle).exists)
          .check(regex(VehicleDetailsPlaybackHeading).exists)
          .check(regex("${expected_registrationNumberFormatted}").exists)
          .check(regex("${expected_make}").exists)
          .check(regex("${expected_model}").exists)
      )
    )
  }

  def taxOrSornVehicle = {
    val url = "/vehicle-tax-or-sorn"
    val chainTitle = s"POST $url"
    exitBlockOnFail(
      exec(
        http(chainTitle)
          .post(url)
          .headers(headers_x_www_form_urlencoded)
          .formParam("csrf_prevention_token", "${csrf_prevention_token}")
          .formParam("action", "")
          .formParam("select", "S")
          .formParam("sornVehicle", "true")
          // Assertions
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          .check(regex(CompleteAndConfirmPageTitle).exists)
      )
    )
  }

  def completeAndConfirmSubmit = {
    val url = "/complete-and-confirm"
    val chainTitle = s"POST $url"
    import java.util.Calendar
    val year = Calendar.getInstance.get(Calendar.YEAR).toString
    val expectedTransactionDate = "${dateDay}/${dateMonth}/" + year
    exitBlockOnFail(
      exec(
        http(chainTitle)
          .post(url)
          .headers(headers_x_www_form_urlencoded)
          .formParam("mileage", "${mileage}")
          .formParam("dateofsale.day", "${dateDay}")
          .formParam("dateofsale.month", "${dateMonth}")
          .formParam("dateofsale.year", year)
          .formParam("Consent", "${consent}")
          .formParam("csrf_prevention_token", "${csrf_prevention_token}")
          .formParam("action", "")
          // Assertions
          .check(regex( """<input type="hidden" name="csrf_prevention_token" value="(.*)"/>""").saveAs("csrf_prevention_token"))
          .check(regex(SummaryPageTitle).exists)
          .check(regex(TransactionDetailsPlaybackHeading).exists)
          .check(regex("${expected_transactionId}").exists)
          .check(regex(expectedTransactionDate).exists)
      )
    )
  }

  def vehicleLookupUnsuccessfulSubmit = {
    val url = "/vehicle-lookup"
    val chainTitle = s"POST $url"
    exitBlockOnFail(
      feed(data)
        .exec(
          http(chainTitle)
            .post(url)
            .headers(headers_x_www_form_urlencoded)
            .formParam("vehicleRegistrationNumber", "${vehicleRegistrationNumber}")
            .formParam("documentReferenceNumber", "${documentReferenceNumber}")
            .formParam("vehicleSoldTo", "${vehicleSoldTo}")
            .formParam("csrf_prevention_token", "${csrf_prevention_token}")
            .formParam("action", "")
            // Assertions
            .check(regex(VehicleLookupFailurePageTitle).exists)
        )
    )
  }
}
