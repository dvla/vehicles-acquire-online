package models

import uk.gov.dvla.vehicles.presentation.common.mappings.Consent.consent
import models.AcquireCacheKeyPrefix.CookiePrefix
import org.joda.time.LocalDate
import play.api.data.Forms.mapping
import play.api.data.validation.{Valid, ValidationError, Invalid, Constraint}
import play.api.i18n.Messages
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Date.{dateMapping, notBefore, notInTheFuture}
import uk.gov.dvla.vehicles.presentation.common.mappings.Mileage.mileage
import uk.gov.dvla.vehicles.presentation.common.services.DateService

case class CompleteAndConfirmFormModel(mileage: Option[Int],
                                       dateOfSale: LocalDate,
                                       consent: String)

object CompleteAndConfirmFormModel {
  implicit val JsonFormat = Json.format[CompleteAndConfirmFormModel]
  final val AllowGoingToCompleteAndConfirmPageCacheKey = s"${CookiePrefix}allowGoingToCompleteAndConfirmPage"
  final val CompleteAndConfirmCacheKey = s"${CookiePrefix}completeAndConfirm"
  final val CompleteAndConfirmCacheTransactionIdCacheKey = s"${CookiePrefix}completeAndConfirmTransactionId"
  implicit val Key = CacheKey[CompleteAndConfirmFormModel](CompleteAndConfirmCacheKey)

  object Form {

    def notOne(message: String = Messages("error.date.notOne"),
               name: String = "constraint.notOne" ) = Constraint[LocalDate](name) {
      case d: LocalDate =>
        if (d.getYear ==  0) Invalid(ValidationError(message))
        else Valid
    }

    final val MileageId = "mileage"
    final val DateOfSaleId = "dateofsale"
    final val TodaysDateId = "todays_date"
    final val ConsentId = "Consent"

    final val validYearsInThePast = 5

    final def detailMapping(implicit dateService: DateService) = mapping(
      MileageId -> mileage,
      DateOfSaleId -> dateMapping
        .verifying(notBefore(new LocalDate().minusYears(validYearsInThePast)))
        .verifying(notInTheFuture())
        .verifying(notOne()),
      ConsentId -> consent
    )(CompleteAndConfirmFormModel.apply)(CompleteAndConfirmFormModel.unapply)
  }
}
