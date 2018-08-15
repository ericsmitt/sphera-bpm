package sphera.bpm.storage.definition

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.json._
import sphera.bpm.storage.NewStorageManager
import io.circe.Json

class PatchDefSpec extends TestKit(ActorSystem("PatchDefSpec")) with NewStorageManager with NewDefStorage {
  "A PatchDef" must {
    "patchDef" in {
      val data = Json.obj(
        "value" -> Json.obj(
          "phone" -> Json.obj("value" -> "+79163333333".asJson)))
      for {
        r1 <- newBpm()
        r2 <- newDefStorageBuilder(r1).build(processDataStructureId, data)
      } yield {
        val updated = r2.patchDef(documentDefPatch)
        //println(updated)
        //println(updated.getArrayDefOpt("users.*.age > 29"))
        succeed
      }
    }
  }
}