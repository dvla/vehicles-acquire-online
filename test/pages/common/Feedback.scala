package pages.common

object Feedback {
final val AcquireEmailFeedbackLink = s"""<a id="${views.common.ProtoType.FeedbackId}" href="${controllers.routes.FeedbackController.present()}" target="_blank">"""
}
