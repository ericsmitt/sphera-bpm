package sphera.bpm.storage

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import sphera.bpm.Bpm
import sphera.bpm.task.Task
import io.circe.Json

import scala.concurrent.Future
import sphera.bpm.json._
import sphera.bpm.Implicits._
import sphera.bpm.storage.document.{ Document, DocumentStorage }

class StorageManagerSpec extends TestKit(ActorSystem("StorageManagerSpec")) with NewStorageManager {
  "A StorageManager" when {
    "DefStorage" must {
      //      "getDef" in {
      //        val data = Json.obj(
      //          "value" -> Json.obj(
      //            "phone" -> Json.obj("value" -> generatePhone.asJson)))
      //        for {
      //          r1 <- newBpm()
      //          r2 <- newDefStorageBuilder(r1).build(processDataStructureId, data)
      //          r3 <- r1.createNode(generateCreateNodeInfo(r2))
      //          r4 <- r1.writeDefValueOnly(r3.id, documentDef) // write to root
      //          r5 <- r1.getDef(r3.id)
      //        } yield succeed
      //      }
      //      "retrieveDef" in {
      //        val data = Json.obj(
      //          "value" -> Json.obj(
      //            "phone" -> Json.obj("value" -> generatePhone.asJson)))
      //        for {
      //          r1 <- newBpm()
      //          r2 <- newDefStorageBuilder(r1).build(processDataStructureId, data)
      //          r3 <- r1.createNode(generateCreateNodeInfo(r2))
      //          r4 <- r1.retrieveDef(r3.id)
      //        } yield succeed
      //      }
      //      "writeDefValueOnly" in {
      //        val data = Json.obj(
      //          "value" -> Json.obj(
      //            "phone" -> Json.obj("value" -> generatePhone.asJson)))
      //        for {
      //          r1 <- newBpm()
      //          r2 <- newDefStorageBuilder(r1).build(processDataStructureId, data)
      //          r3 <- r1.createNode(generateCreateNodeInfo(r2))
      //          r4 <- r1.writeDefValueOnly(r3.id, documentDef) // write to root
      //          r5 <- r1.retrieveDef(r3.id)
      //        } yield succeed
      //      }

      "patchDef" in {
        val data = Json.obj(
          "value" -> Json.obj(
            "phone" -> Json.obj("value" -> generatePhone.asJson)))
        for {
          r1 <- newBpm()
          r2 <- newDefStorageBuilder(r1).build(processDataStructureId, data)
          r3 <- r1.createNode(generateCreateNodeInfo(r2))
          r4 <- r1.writeDefValueOnly(r3.id, documentDef) // write to root
          r5 <- r1.patchDef(r3.id, documentDefPatch)
          r6 <- r1.retrieveDef(r3.id)
        } yield {
          println(documentDefPatch)
          println(r6)

          r6.getDefStringValue("phone") shouldBe "x"
          r6.getDescription("phone") should contain("x")
          r6.getDefBooleanValue("approved") shouldBe false
          r6.getDefBooleanValue("changeable") shouldBe true
          r6.getDefStringValue("title.en") shouldBe "x"
          r6.getDefStringValue("title.ru") shouldBe "x"
          r6.getDescription("title.en") should contain("x")
          r6.getDescription("title.ru") should contain("x")
        }
      }
    }
  }
}