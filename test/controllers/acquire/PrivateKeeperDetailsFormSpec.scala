package controllers.acquire

import helpers.UnitSpec
import controllers.PrivateKeeperDetails
import pages.acquire.PrivateKeeperDetailsPage.TitleValid
import viewmodels.PrivateKeeperDetailsViewModel.Form.TitleId

class PrivateKeeperDetailsFormSpec extends UnitSpec {

  "form" should {
    "accept if form is completed with all fields correctly" in {
      val model = formWithValidDefaults(
        title = TitleValid).get
      model.title should equal(TitleValid)
    }

    "accept if form is completed with mandatory fields only" in {
      val model = formWithValidDefaults(
        title = TitleValid).get
      model.title should equal(TitleValid)
    }

    "reject if form has no fields completed" in {
      formWithValidDefaults(title = "").errors should have length 1
    }
  }

  "title" should {
    "reject if no selection is made" in {
      formWithValidDefaults(title = "").errors should have length 1
    }

    "accept if title is selected" in {
      val model = formWithValidDefaults(
        title = TitleValid).get
      model.title should equal(TitleValid)
    }
  }

  private def formWithValidDefaults(title: String = TitleValid) = {
    injector.getInstance(classOf[PrivateKeeperDetails])
      .form.bind(
        Map(
          TitleId -> title
        )
      )
  }
}
