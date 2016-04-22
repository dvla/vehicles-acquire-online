package views.acquire

import composition.TestHarness
import helpers.acquire.CookieFactoryForUISpecs
import helpers.tags.UiTag
import helpers.UiSpec
import org.scalatest.selenium.WebBrowser.click
import org.scalatest.selenium.WebBrowser.go
import org.scalatest.selenium.WebBrowser.pageSource
import org.scalatest.selenium.WebBrowser.pageTitle
import pages.acquire.{BeforeYouStartPage, SetupTradeDetailsPage}
import pages.acquire.BeforeYouStartPage.startNow
import pages.common.AlternateLanguages._
import pages.common.Feedback.AcquireEmailFeedbackLink


class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {
  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      pageTitle should equal(BeforeYouStartPage.title)
    }

    "contain feedback email facility with appropriate subject" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      pageSource.contains(AcquireEmailFeedbackLink) should equal(true)
    }

    //TODO uncomment when Welsh translation complete
/*
    "display the 'Cymraeg' language button and not the 'English' language button when the play language cookie has " +
      "value 'en'" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage // By default will load in English.
      CookieFactoryForUISpecs.withLanguageEn()
      go to BeforeYouStartPage

      isCymraegDisplayed should equal(true)
      isEnglishDisplayed should equal(false)
    }

    "display the 'English' language button and not the 'Cymraeg' language button when the play language cookie has "  +
      "value 'cy'" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage // By default will load in English.
      CookieFactoryForUISpecs.withLanguageCy()
      go to BeforeYouStartPage

      isCymraegDisplayed should equal(false)
      isEnglishDisplayed should equal(true)
      pageTitle should equal(BeforeYouStartPage.titleCy)
    }

    "display the 'Cymraeg' language button and not the 'English' language button and mailto when the play language " +
      "cookie does not exist (assumption that the browser default language is English)" taggedAs UiTag in
      new WebBrowserForSelenium {
      go to BeforeYouStartPage

      isCymraegDisplayed should equal(true)
      isEnglishDisplayed should equal(false)
    }
*/

  }

  "startNow button" should {
    "go to next page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      click on startNow
      pageTitle should equal(SetupTradeDetailsPage.title)
    }
  }
}