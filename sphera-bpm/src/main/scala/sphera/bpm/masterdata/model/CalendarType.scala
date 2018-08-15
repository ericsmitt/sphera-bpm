package sphera.bpm.masterdata.model

import java.time.{ LocalDate, LocalTime }
import java.util.UUID

import sphera.core.domain.tenancy.model.User

case class CalendarType(
  id: CalendarType.Id,
  name: String,
  description: Option[String],
  deviations: Set[UnOrdinaryDay],
  modifyAttr: ModifyAttr,
  schedule: Schedule = Schedule(LocalTime.of(9, 0), LocalTime.of(18, 0), LocalTime.of(12, 0), LocalTime.of(13, 0)))

object CalendarType {
  type Id = UUID
  type CalendarTypes = Map[CalendarType.Id, CalendarType]
}

case class UpdateCalendarType(
  id: CalendarType.Id,
  name: String,
  description: Option[String],
  deviations: Set[UnOrdinaryDay],
  userId: User.Id)

case class UnOrdinaryDay(
  day: LocalDate,
  name: String,
  description: Option[String],
  isWorkingDay: Boolean = false,
  period: (LocalDate, LocalDate) = (LocalDate.of(1990, 1, 1), LocalDate.of(2090, 1, 1)))

case class Schedule(
  startWorkTime: LocalTime,
  endWorkingTime: LocalTime,
  startLunchTime: LocalTime,
  endLunchTime: LocalTime)