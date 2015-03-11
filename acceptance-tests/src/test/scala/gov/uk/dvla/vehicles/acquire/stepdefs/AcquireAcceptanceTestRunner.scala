package gov.uk.dvla.vehicles.acquire.stepdefs

import cucumber.api.junit.Cucumber
import cucumber.api.CucumberOptions
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/gherkin"),
  glue = Array("gov.uk.dvla.vehicles.acquire.stepdefs"),
  tags = Array("@working","~@Ignore")
)
class AcquireAcceptanceTestRunner {
}
