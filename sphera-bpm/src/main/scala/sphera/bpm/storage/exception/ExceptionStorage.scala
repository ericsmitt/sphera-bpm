package sphera.bpm.storage.exception

import java.time.ZonedDateTime

import sphera.bpm.BpmException
import sphera.bpm.storage.Storage

case class ExceptionStorage(exceptions: Map[ZonedDateTime, BpmException]) {
  def persistException(exception: BpmException): ExceptionStorage = {
    copy(exceptions + (exception.zonedDateTime -> exception))
  }
}

object ExceptionStorage {
  trait Command extends Storage.Command
  trait Request extends Storage.Request
  trait Response extends Storage.Response
  trait Event extends Storage.Event

  case object GetExceptions extends Request
  case class PersistException(exception: BpmException) extends Request

  case class AddedExceptionEvt(exception: BpmException) extends Event

  case class ExceptionsMap(exceptions: Map[ZonedDateTime, BpmException]) extends Response

  def empty: ExceptionStorage = ExceptionStorage(Map.empty)
}

