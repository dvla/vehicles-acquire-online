package pages.common

import uk.gov.dvla.vehicles.presentation.common.views.widgets.Prototype.FeedbackId

object Feedback {
  final val AcquireEmailFeedbackLink = s"""<a id="${FeedbackId}" href="${controllers.routes.FeedbackController.present()}" target="_blank">"""
}
