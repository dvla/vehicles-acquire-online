package controllers

import com.google.inject.Inject
import models.AcquireCacheKeyPrefix.CookiePrefix
import play.api.mvc.{Request, Result}
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.BusinessKeeperDetailsViewModel
import uk.gov.dvla.vehicles.presentation.common.controllers.BusinessKeeperDetailsBase

class BusinessKeeperDetails @Inject()()(implicit protected override val clientSideSessionFactory: ClientSideSessionFactory,
                                            val config: Config) extends BusinessKeeperDetailsBase {
  protected override def presentResult(model: BusinessKeeperDetailsViewModel)(implicit request: Request[_]): Result =
    Ok(views.html.acquire.business_keeper_details(model))

  protected def invalidFormResult(model:BusinessKeeperDetailsViewModel)(implicit request: Request[_]): Result =
    BadRequest(views.html.acquire.business_keeper_details(model))

  protected def missingVehicleDetails(implicit request: Request[_]): Result =
   Redirect(routes.SetUpTradeDetails.present())

  protected def success(implicit request: Request[_]): Result = Redirect(routes.NewKeeperChooseYourAddress.present())
}
