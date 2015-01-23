package gov.uk.dvla.vehicles.acquire.acceptancetest

import cucumber.api.junit.Cucumber
import cucumber.api.junit.Cucumber.Options
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@Options(
  features = Array("acceptance-tests/src/test/resources/gherkin/US1022.feature"),
  glue = Array("gov.uk.dvla.vehicles.acquire.stepdefs"),
  tags = Array("@tag")
)
class US1022AcceptanceTest
