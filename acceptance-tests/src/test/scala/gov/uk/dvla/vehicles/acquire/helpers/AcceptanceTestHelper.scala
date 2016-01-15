package gov.uk.dvla.vehicles.acquire.helpers

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.Matchers
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WithClue
import uk.gov.dvla.vehicles.presentation.common.testhelpers.ScaleFactor

trait AcceptanceTestHelper
  extends ScalaDsl
  with EN
  with Matchers
  with WithClue
  with ScaleFactor
  with IntegrationPatience
