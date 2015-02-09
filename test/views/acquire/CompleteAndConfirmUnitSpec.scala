package views.acquire

import uk.gov.dvla.vehicles.presentation.common.UnitSpec

class CompleteAndConfirmUnitSpec extends UnitSpec {

  "modify" should {
    "return the same map when no modification is specified" in {
      val result = CompleteAndConfirm.modify(htmlArgs, modify = false)
      result should equal(htmlArgs)
    }

    "return a modified map with autofocus removed and tabindex added when modification is specified" in {
      val result = CompleteAndConfirm.modify(htmlArgs, modify = true)
      result should equal(Map('tabindex -> -1))
    }
  }

  private def htmlArgs: Map[Symbol, Any] = Map('autofocus -> true)
}
