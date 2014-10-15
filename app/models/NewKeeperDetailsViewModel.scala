package models

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.mappings.TitleType
import org.joda.time.LocalDate


final case class NewKeeperDetailsViewModel(title: Option[TitleType],
                                           firstName: Option[String],
                                           lastName: Option[String],
                                           dateOfBirth: Option[LocalDate],
                                           driverNumber: Option[String],
                                           businessName: Option[String],
                                           fleetNumber: Option[String],
                                           email: Option[String],
                                           address: AddressModel,
                                           isBusinessKeeper: Boolean,
                                           displayName: String)

object NewKeeperDetailsViewModel {
  implicit val JsonFormatTitleType = Json.format[TitleType]
  implicit val JsonFormat = Json.format[NewKeeperDetailsViewModel]
  final val NewKeeperDetailsCacheKey = "newKeeperDetails"
  implicit val Key = CacheKey[NewKeeperDetailsViewModel](value = NewKeeperDetailsCacheKey)
}
