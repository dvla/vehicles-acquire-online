package pages.common

object Feedback {
final val AcquireEmailFeedbackLink = s"""<a id="${views.common.Feedback.FeedbackId}" href="${controllers.routes.FeedbackController.present()}" target="blank">"""
 // final val AcquireEmailFeedbackLink = s"""<a id="${views.common.Feedback.FeedbackId}" href="" onclick="window.open('${controllers.routes.FeedbackController.present()}', '_blank')">"""
}
