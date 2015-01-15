package controllers

import com.google.inject.Inject
import models.BusinessKeeperDetailsFormModel
import play.api.data.{FormError, Form}
import play.api.mvc.{Request, Result, Action, Controller}
import play.api.Logger
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.model.VehicleAndKeeperDetailsModel
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.views.helpers.FormExtensions.formBinding
import models.NewKeeperChooseYourAddressFormModel.NewKeeperChooseYourAddressCacheKey
import models.BusinessKeeperDetailsViewModel

class BusinessKeeperDetails @Inject()()(implicit protected override val clientSideSessionFactory: ClientSideSessionFactory,
                                            protected override val config: Config) extends BusinessKeeperDetailsBase {
  protected override def presentResult(model: BusinessKeeperDetailsViewModel)(implicit request: Request[_]): Result =
    Ok(views.html.acquire.business_keeper_details(model))

  protected def error1(model:BusinessKeeperDetailsViewModel)(implicit request: Request[_]): Result =
    BadRequest(views.html.acquire.business_keeper_details(model))

  protected def error2(implicit request: Request[_]): Result =
   Redirect(routes.SetUpTradeDetails.present())

  protected def success(implicit request: Request[_]): Result = Redirect(routes.NewKeeperChooseYourAddress.present())
}
