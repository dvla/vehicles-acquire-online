package controllers

import models._
import views.html.acquire.business_keeper_details_complete
import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import play.api.data.Form
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichForm, RichResult}
import play.api.Logger

class BusinessKeeperDetailsComplete @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                               config: Config) extends Controller {

  private[controllers] val form = Form(
    BusinessKeeperDetailsCompleteFormModel.Form.Mapping
  )

  def present = Action { implicit request =>

    request.cookies.getModel[BusinessKeeperDetailsFormModel] match {
      case Some(businessKeeperDetails) => {
        Ok(business_keeper_details_complete(BusinessKeeperDetailsCompleteViewModel(
          form.fill(), null, null
        )))
      }
      case _ =>
        Logger.warn("Did not find BusinessKeeperDetails cookie. Now redirecting to SetUpTradeDetails.")
        Redirect(routes.SetUpTradeDetails.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm =>
        BadRequest(business_keeper_details_complete(BusinessKeeperDetailsCompleteViewModel(
          invalidForm, null, null
        ))),
      validForm => Redirect(routes.NotImplemented.present()).withCookie(validForm)
    )
  }
}
