package models

import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import models.AcquireCacheKeyPrefix.CookiePrefix

final case class CompleteAndConfirmResponseModel(transactionId: String,
                                                 transactionTimestamp: DateTime)

object CompleteAndConfirmResponseModel {
  implicit val JsonFormat = Json.format[CompleteAndConfirmResponseModel]
  final val AcquireCompletionResponseCacheKey = s"${CookiePrefix}acquireCompletionResponse"
  implicit val Key = CacheKey[CompleteAndConfirmResponseModel](AcquireCompletionResponseCacheKey)
}
