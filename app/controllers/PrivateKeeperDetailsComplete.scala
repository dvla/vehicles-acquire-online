package controllers

import com.google.inject.Inject
import play.api.data.Form
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichForm, RichResult}
import utils.helpers.Config
import viewmodels.{PrivateKeeperDetailsCompleteFormModel, PrivateKeeperDetailsCompleteViewModel}
import views.html.acquire.private_keeper_details_complete

class PrivateKeeperDetailsComplete @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                               config: Config) extends Controller {

  private[controllers] val form = Form(
    PrivateKeeperDetailsCompleteFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    Ok(private_keeper_details_complete(PrivateKeeperDetailsCompleteViewModel(
      form.fill(), null, null
    )))
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => BadRequest(private_keeper_details_complete(PrivateKeeperDetailsCompleteViewModel(
        invalidForm, null, null
      ))),
      validForm => Redirect(routes.NotImplemented.present()).withCookie(validForm)
    )
  }
}
