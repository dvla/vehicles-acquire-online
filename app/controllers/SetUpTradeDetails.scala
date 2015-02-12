package controllers

import com.google.inject.Inject
import play.api.data.Form
import play.api.mvc.{Result, Request}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.controllers.SetUpTradeDetailsBase
import common.model.SetupTradeDetailsFormModel

import utils.helpers.Config
import models.AcquireCacheKeyPrefix.CookiePrefix

class SetUpTradeDetails @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                          config: Config) extends SetUpTradeDetailsBase {

  override def presentResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result =
    Ok(views.html.acquire.setup_trade_details(model))

  override def invalidFormResult(model: Form[SetupTradeDetailsFormModel])(implicit request: Request[_]): Result =
    BadRequest(views.html.acquire.setup_trade_details(model))

  override def success(implicit request: Request[_]): Result =
    Redirect(routes.BusinessChooseYourAddress.present())

}