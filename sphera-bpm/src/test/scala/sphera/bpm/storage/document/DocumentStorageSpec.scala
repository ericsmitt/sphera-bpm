package sphera.bpm.storage.document

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.json._
import sphera.bpm.storage.NewStorageManager
import io.circe.Json

class DocumentStorageSpec extends TestKit(ActorSystem("DocumentStorageSpec")) with NewDocumentStorage {
  "DocumentStorage" must {
    "getDocument" in {
      val data = Json.obj(
        "value" -> Json.obj(
          "phone" -> Json.obj("value" -> "+79169163333331".asJson)))
      for {
        r1 <- newBpm()
        r2 <- newDefStorageBuilder(r1).build(processDataStructureId, data)
        r3 <- r1.createNode(generateCreateNodeInfo(r2))
        r4 <- r1.getDocument(r3.id)
      } yield succeed
    }
    //    "updateDocument" in {
    //      val data = Json.obj(
    //        "value" -> Json.obj(
    //          "phone" -> Json.obj("value" -> "+79163333331".asJson)))
    //      for {
    //        r1 <- newBpm()
    //        r2 <- newDefStorageBuilder(r1).build(processDataStructureId, data)
    //        r3 <- r1.createNode(generateCreateNodeInfo(r2))
    //        r4 <- r1.updateDocument(r3.id, UpdateDocument(document))
    //        r5 <- r1.getDocument(r3.id)
    //      } yield {
    //        println(r5)
    //        succeed
    //      }
    //    }
  }
}