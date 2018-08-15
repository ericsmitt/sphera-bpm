package sphera.bpm.storage.document

import java.time.{ LocalDateTime, ZonedDateTime }
import java.util.concurrent.TimeUnit

import sphera.bpm.storage.definition.{ Def, Description, Name, Value }
import sphera.core.akkaext.actor.{ DataPath, Path }
import io.circe.Json
import sphera.bpm.storage.Node
import sphera.bpm.json._
import scala.concurrent.duration.{ Duration, FiniteDuration }

case class Document(
  body: Json,
  path: Path,
  dateTime: ZonedDateTime = ZonedDateTime.now(),
  genTime: FiniteDuration = FiniteDuration(1.toLong, TimeUnit.MILLISECONDS)) {

  override def toString = this.asJsonStr
}

case class UpdateDocument(body: Json)