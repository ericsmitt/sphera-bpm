package sphera.bpm.json.storage

import sphera.bpm.json.storage.definition.PatchDefCodec
import sphera.bpm.json.storage.document.DocumentCodec
import sphera.bpm.storage.definition.DefStorage._
import io.circe.Encoder
import sphera.bpm.json._
import sphera.bpm.storage.Storage.Event
import sphera.bpm.storage.json.JsonStorage._

trait StorageCodec extends PatchDefCodec with DocumentCodec {
  implicit val encodeEvent: Encoder[Event] = {
    case x: UpdatedJsonEvt => x.asJson
    case x: AddedJsonEvt => x.asJson
    case x: DeletedJsonEvt => x.asJson
    case x: UpdatedDefValueEvt => x.asJson
    case x: UpdatedDefValueOnlyEvt => x.asJson
    case x: CreatedDefEvt => x.asJson
    case x: UpdatedDefEvt => x.asJson
    case x: DeletedDefEvt => x.asJson
  }
}
