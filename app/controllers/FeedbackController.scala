package controllers

import com.google.inject.Inject
import play.api.data.{FormError, Form}
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Call, Controller}
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.ClientSideSessionFactory
import common.clientsidesession.CookieImplicits.RichCookies
import common.controllers.FeedbackBase
import common.model.FeedbackForm
import common.model.FeedbackForm.Form.{emailMapping, nameMapping, feedback}
import common.services.DateService
import common.views.helpers.FormExtensions.formBinding
import common.webserviceclients.emailservice.EmailService
import common.webserviceclients.healthstats.HealthStats
import utils.helpers.Config

class FeedbackController @Inject()(val emailService: EmailService,
                                   val dateService: DateService,
                                   val healthStats: HealthStats)
                                  (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                   config: Config) extends Controller with FeedbackBase {

  override val emailConfiguration = config.emailConfiguration

  private[controllers] val form = Form(
    FeedbackForm.Form.Mapping
  )

  implicit val controls: Map[String, Call] = Map(
    "submit" -> controllers.routes.FeedbackController.submit()
  )

  def present() = Action { implicit request =>
    Ok(views.html.acquire.feedback(form))
  }

  def submit: Action[AnyContent] = Action { implicit request =>
    form.bindFromRequest.fold (
      invalidForm => BadRequest(views.html.acquire.feedback(formWithReplacedErrors(invalidForm))),
        validForm => {
          val trackingId = request.cookies.trackingId
          sendFeedback(validForm, Messages("common_feedback.subject"), trackingId)
          Ok(views.html.acquire.feedbackSuccess())
        }
    )
  }

  private def formWithReplacedErrors(form: Form[FeedbackForm]) = {
    form.replaceError(
      feedback, FormError(key = feedback,message = "error.feedback", args = Seq.empty)
    ).replaceError(
        nameMapping, FormError(key = nameMapping, message = "error.feedbackName", args = Seq.empty)
      ).replaceError(
        emailMapping, FormError(key = emailMapping, message = "error.email", args = Seq.empty)
      ).distinctErrors
  }
}