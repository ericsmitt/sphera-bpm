package sphera.bpm.json.storage.definition

import sphera.bpm.Implicits._
import sphera.bpm.storage.definition._
import io.circe._
import io.circe.syntax._

trait ReadDefCodec extends DefCodec {
  implicit val encodeReadDef: Encoder[ReadDef] = (x: ReadDef) => {
    // adds patch mark
    x.value.addJson("patch", Patch.Read.value.asJson).repr.asJson
  }
}