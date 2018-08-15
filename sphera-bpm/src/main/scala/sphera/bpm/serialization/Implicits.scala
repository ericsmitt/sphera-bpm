package sphera.bpm.serialization

import java.time.ZonedDateTime
import java.util.UUID

import scala.concurrent.duration.{ Duration, FiniteDuration }

object Implicits {
  implicit def longToFiniteDuration(x: Long): FiniteDuration = Duration.fromNanos(x)

  implicit def finiteDurationToLong(x: FiniteDuration): Long = x.toNanos

  implicit def stringToZonedDateTime(x: String): ZonedDateTime = ZonedDateTime.parse(x)

  implicit def stringOptToZonedDateTimeOpt(x: Option[String]): Option[ZonedDateTime] =
    typeAToOptionTypeB(x)

  implicit def longOptToFiniteDurationOpt(x: Option[Long])(implicit convert: Long => FiniteDuration): Option[FiniteDuration] =
    typeAToOptionTypeB(x)

  implicit def finiteDurationOptToLongOpt(x: Option[FiniteDuration])(implicit convert: FiniteDuration => Long): Option[Long] =
    typeAToOptionTypeB(x)

  implicit def zonedDateTimeToString(x: ZonedDateTime): String = x.toString

  implicit def zonedDateTimeOptToStringOpt(x: Option[ZonedDateTime]): Option[String] =
    typeAToOptionTypeB(x)

  implicit def stringToUUID(x: String): UUID = UUID.fromString(x)

  implicit def stringOptToUUIDOpt(x: Option[String]): Option[UUID] = typeAToOptionTypeB(x)

  implicit def UUIDOptToStringOpt(x: Option[UUID]): Option[String] = typeAToOptionTypeB(x)

  implicit def UUIDToString(x: UUID): String = x.toString

  implicit def UUIDSetToStringSeq(x: Set[UUID]): Seq[String] = x.map(UUIDToString)

  implicit def stringSeqToUUIDSet(x: Seq[String]): Set[UUID] = toSet(x)

  implicit def stringToBigDecimal(x: String): BigDecimal = BigDecimal(x)

  implicit def toOption[T](x: T): Option[T] = Option(x)

  implicit def toSeq[A, B](x: Set[A])(implicit convert: A => B): Seq[B] = x.map(convert).toSeq

  implicit def toSet[A, B](x: Seq[A])(implicit convert: A => B): Set[B] = x.map(convert).toSet

  implicit def toSet[A, B](x: Set[A])(implicit convert: A => B): Set[B] = x map convert

  private implicit def typeAToOptionTypeB[A, B](x: Option[A])(implicit convert: A => B): Option[B] =
    x map convert
}
