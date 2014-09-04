package viewmodels

import play.api.data.Mapping
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.views.constraints.BusinessName
import common.views.helpers.FormExtensions.nonEmptyTextWithTransform
import common.clientsidesession.CacheKey

final case class BusinessKeeperDetailsFormViewModel(businessName: String)

object BusinessKeeperDetailsFormViewModel {
  implicit val JsonFormat = Json.format[BusinessKeeperDetailsFormViewModel]
  final val BusinessKeeperDetailsCacheKey = "businessKeeperDetails"
  implicit val Key = CacheKey[BusinessKeeperDetailsFormViewModel](BusinessKeeperDetailsCacheKey)

  object Form {
    final val BusinessNameId = "businessName"
    final val BusinessNameMinLength = 2
    final val BusinessNameMaxLength = 56

    private final val BusinessNameMapping: Mapping[String] =
      nonEmptyTextWithTransform(_.trim)(BusinessNameMinLength, BusinessNameMaxLength)
        .verifying(BusinessName.validBusinessName)

    final val Mapping = mapping(
      BusinessNameId -> BusinessNameMapping
    )(BusinessKeeperDetailsFormViewModel.apply)(BusinessKeeperDetailsFormViewModel.unapply)
  }
}