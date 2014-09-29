package models

import mappings.Consent.consent
import org.joda.time.LocalDate
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.clientsidesession.CacheKey
import common.mappings.Mileage.mileage
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Date.{optionalDateOfBirth, dateMapping, notInTheFuture}
import uk.gov.dvla.vehicles.presentation.common.mappings.Mileage.mileage
import mappings.Consent.consent

case class PrivateKeeperDetailsCompleteFormModel(dateOfBirth: Option[LocalDate],
                                                 mileage: Option[Int],
                                                 dateOfSale: LocalDate,
                                                 consent: String)

object PrivateKeeperDetailsCompleteFormModel {
  implicit val JsonFormat = Json.format[PrivateKeeperDetailsCompleteFormModel]
  final val PrivateKeeperDetailsCompleteCacheKey = "privateKeeperDetailsComplete"
  implicit val Key = CacheKey[PrivateKeeperDetailsCompleteFormModel](PrivateKeeperDetailsCompleteCacheKey)

  object Form {
    final val DateOfBirthId = "privatekeeper_dateofbirth"
    final val MileageId = "privatekeeper_mileage"
    final val DateOfSaleId = "privatekeeper_dateofsale"
    final val ConsentId = "consent"

    final val Mapping = mapping(
      DateOfBirthId -> optionalDateOfBirth,
      MileageId -> mileage,
      DateOfSaleId -> dateMapping.verifying(notInTheFuture()),
      ConsentId -> consent
    )(PrivateKeeperDetailsCompleteFormModel.apply)(PrivateKeeperDetailsCompleteFormModel.unapply)
  }
}