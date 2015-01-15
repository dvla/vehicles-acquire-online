package controllers

import com.google.inject.Inject
import models.BusinessKeeperDetailsFormModel
import play.api.data.{FormError, Form}
import play.api.mvc.{Request, Result, Action, Controller}
import play.api.Logger
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import common.clientsidesession.CookieImplicits.{RichCookies, RichForm, RichResult}
import common.views.helpers.FormExtensions.formBinding
import models.NewKeeperChooseYourAddressFormModel.NewKeeperChooseYourAddressCacheKey
import models.BusinessKeeperDetailsViewModel

abstract class BusinessKeeperDetailsBase @Inject()()
                    (implicit protected val clientSideSessionFactory: ClientSideSessionFactory,
                              protected val config: Config) extends Controller {

  protected def presentResult(model: BusinessKeeperDetailsViewModel)(implicit request: Request[_]): Result

  protected def error1(model:BusinessKeeperDetailsViewModel)(implicit request: Request[_]): Result

  protected def error2(implicit request: Request[_]): Result

  protected def success(implicit request: Request[_]): Result

  protected[controllers] val form = Form(
    BusinessKeeperDetailsFormModel.Form.Mapping
  )

  private final val CookieErrorMessage = "Did not find VehicleDetailsModel cookie. Now redirecting to SetUpTradeDetails."

  def present = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
      case Some(vehicleAndKeeeprDetails) => presentResult(BusinessKeeperDetailsViewModel(form.fill(),vehicleAndKeeeprDetails))
      case _ => redirectToSetupTradeDetails(CookieErrorMessage)
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
          case Some(vehicleAndKeeperDetails) =>
            error1(BusinessKeeperDetailsViewModel(formWithReplacedErrors(invalidForm), vehicleAndKeeperDetails))
          case None => redirectToSetupTradeDetails(CookieErrorMessage)
        }
      },
      validForm => success
       .withCookie(validForm)
       .discardingCookie(NewKeeperChooseYourAddressCacheKey)
    )
  }

  private def formWithReplacedErrors(form: Form[BusinessKeeperDetailsFormModel]) = {
    form.replaceError(
      BusinessKeeperDetailsFormModel.Form.BusinessNameId,
      FormError(key = BusinessKeeperDetailsFormModel.Form.BusinessNameId,message = "error.validBusinessKeeperName")
    ).replaceError(
        BusinessKeeperDetailsFormModel.Form.EmailId,
        FormError(key = BusinessKeeperDetailsFormModel.Form.EmailId,message = "error.email")
      ).replaceError(
        BusinessKeeperDetailsFormModel.Form.PostcodeId,
        FormError(key = BusinessKeeperDetailsFormModel.Form.PostcodeId,message = "error.restricted.validPostcode")
      ).distinctErrors
  }

  private def redirectToSetupTradeDetails(message:String)(implicit request: Request[_]) = {
    Logger.warn(message)
    error2
  }

}
