package controllers

import composition.WithApplication
import helpers.UnitSpec
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.NewKeeperChooseYourAddressFormModel.Form.AddressSelectId
import uk.gov.dvla.vehicles.presentation.common.services.DateServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddressNotFound
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddressNotFound
import webserviceclients.fakes.FakeAddressLookupWebServiceImpl.UprnValid

class NewKeeperChooseYourAddressFormSpec extends UnitSpec {
  "form" should {
    "accept when all fields contain valid responses" in new WithApplication {
      formWithValidDefaults().get.uprnSelected should equal(UprnValid.toString)
    }
  }

  "addressSelect" should {
    "reject if empty" in new WithApplication {
      val errors = formWithValidDefaults(addressSelected = "").errors
      errors.length should equal(1)
      errors(0).key should equal(AddressSelectId)
      errors(0).message should equal("error.required")
    }
  }

  private def businessChooseYourAddressWithFakeWebService(uprnFound: Boolean = true) = {
    val responsePostcode = if (uprnFound) responseValidForPostcodeToAddress
    else responseValidForPostcodeToAddressNotFound
    val responseUprn = if (uprnFound) responseValidForUprnToAddress else responseValidForUprnToAddressNotFound
    val fakeWebService = new FakeAddressLookupWebServiceImpl(responsePostcode, responseUprn)
    val healthStatsMock = mock[HealthStats]
    when(healthStatsMock.report(anyString)(any[Future[_]])).thenAnswer(new Answer[Future[_]] {
      override def answer(invocation: InvocationOnMock): Future[_] = invocation.getArguments()(1).asInstanceOf[Future[_]]
    })
    val addressLookupService = new AddressLookupServiceImpl(fakeWebService, new DateServiceImpl, healthStatsMock)
    implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
    implicit val config: Config = mock[Config]
    new NewKeeperChooseYourAddress(addressLookupService)
  }

  private def formWithValidDefaults(addressSelected: String = UprnValid.toString) = {
    businessChooseYourAddressWithFakeWebService().form.bind(
      Map(AddressSelectId -> addressSelected)
    )
  }
}