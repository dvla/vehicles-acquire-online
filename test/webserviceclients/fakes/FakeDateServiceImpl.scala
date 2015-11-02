package webserviceclients.fakes

import org.joda.time.{DateTime, Instant}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import webserviceclients.fakes.FakeDateServiceImpl.DateOfAcquisitionDayValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfAcquisitionMonthValid
import webserviceclients.fakes.FakeDateServiceImpl.DateOfAcquisitionYearValid

final class FakeDateServiceImpl extends DateService {

  override def today = DayMonthYear(
    DateOfAcquisitionDayValid.toInt,
    DateOfAcquisitionMonthValid.toInt,
    DateOfAcquisitionYearValid.toInt
  )

  override def now = Instant.now()

  override def dateTimeISOChronology: String = new DateTime(
    DateOfAcquisitionYearValid.toInt,
    DateOfAcquisitionMonthValid.toInt,
    DateOfAcquisitionDayValid.toInt,
    0,
    0).toString
}

object FakeDateServiceImpl {
  final val DateOfAcquisitionDayValid = "25"
  final val DateOfAcquisitionMonthValid = "11"
  final val DateOfAcquisitionYearValid = "1970"
  private final val dateTime = DateTime.now
  final val TodayDay = dateTime.toString("dd")
  final val TodayMonth = dateTime.toString("MM")
  final val TodayYear = dateTime.getYear.toString
}
