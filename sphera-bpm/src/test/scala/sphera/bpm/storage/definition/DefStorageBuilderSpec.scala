package sphera.bpm.storage.definition

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.json._
import sphera.bpm.process.NewDefStorageBuilder
import io.circe.Json

class DefStorageBuilderSpec extends TestKit(ActorSystem("DefStorageBuilder")) with NewDefStorageBuilder {
  "A DefStorageBuilder" when {
    "userDataStructure" must {
      "build" in {
        for {
          r1 <- newDefStorageBuilder()
          r2 <- r1.build(userDataStructureId)
        } yield succeed
      }
    }
    "usersDataStructure" must {
      "build" in {
        for {
          r1 <- newDefStorageBuilder()
          r2 <- r1.build(usersDataStructureId)
        } yield succeed
      }
    }
    "processDataStructure" must {
      "build" in {
        val data = Json.obj(
          "value" -> Json.obj(
            "phone" -> Json.obj("value" -> "+79163333333".asJson)))
        for {
          r1 <- newDefStorageBuilder()
          r2 <- r1.build(processDataStructureId, data)
        } yield succeed
      }
      "findRoleUser" in {
        for {
          r1 <- newDefStorageBuilder()
          r2 <- r1.build(processDataStructureId)
        } yield {
          r2.findRoleUser("candidate") should contain(candidateUser)
          succeed
        }
      }
    }
  }
}