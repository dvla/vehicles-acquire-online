package gov.uk.dvla.vehicles.acquire.runner

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/gherkin/US1446.feature"),
  glue = Array("gov.uk.dvla.vehicles.acquire.stepdefs"),
  tags = Array("@working","~@Ignore")
)
class US1446 {

}
