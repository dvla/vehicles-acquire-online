package viewmodels

import constraints.TraderBusinessName
import uk.gov.dvla.vehicles.presentation.common.mappings.Postcode
import Postcode.postcode
import play.api.data.Forms._
import play.api.data.Mapping
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions
import FormExtensions._
import play.api.data.validation.Constraints
import mappings.Email.email
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class SetupTradeDetailsViewModel(traderBusinessName: String, traderPostcode: String, traderEmail: Option[String])

object SetupTradeDetailsViewModel {
  implicit val JsonFormat = Json.format[SetupTradeDetailsViewModel]
  final val SetupTradeDetailsCacheKey = "setupTraderDetails"
  implicit val Key = CacheKey[SetupTradeDetailsViewModel](SetupTradeDetailsCacheKey)

  object Form {
    final val TraderNameId = "traderName"
    final val TraderPostcodeId = "traderPostcode"
    final val TraderEmailId = "traderEmail"
    final val TraderNameMinLength = 2
    final val TraderNameMaxLength = 58
    final val TraderEmailMinLength = 3
    final val TraderEmailMaxLength = 255

    private final val TraderNameMapping: Mapping[String] =
      nonEmptyTextWithTransform(_.toUpperCase.trim)(TraderNameMinLength, TraderNameMaxLength)
        .verifying(TraderBusinessName.validTraderBusinessName)

    private final val EmailMapping: Mapping[String] =
      email.verifying(Constraints.maxLength(TraderEmailMaxLength))

    final val Mapping = mapping(
      TraderNameId -> TraderNameMapping,
      TraderPostcodeId -> postcode,
      TraderEmailId -> optional(email.verifying(Constraints.nonEmpty))
    )(SetupTradeDetailsViewModel.apply)(SetupTradeDetailsViewModel.unapply)
  }
}
