package gov.uk.dvla.vehicles.acquire

import cucumber.api.junit.Cucumber
import cucumber.api.CucumberOptions
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("acceptance-tests/src/test/resources/"),
  glue = Array("gov.uk.dvla.vehicles.acquire.stepdefs"),
  tags = Array("@working")
)
class AcquireTestRunner