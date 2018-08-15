package sphera.bpm

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.Implicits._
import sphera.bpm.json._
import sphera.bpm.model.ImplicitModifier
import sphera.core.akkaext.actor.RootDataPath
import sphera.core.test.PersistenceSpec

class BpmSpec extends TestKit(ActorSystem("BpmSpec")) with BpmBaseSpec {
  "A Bpm" when {
    "createProcessAndGetId" must {
      "create process" in {
        for {
          bpm <- newBpm()
          r1 <- bpm.createProcessAndGetId(generateCreateProcessInfo())
          //r2 <- bpm.updateProcessJson(r1, path, p2)
          r3 <- bpm.getProcessJson(r1, RootDataPath)
          r4 <- bpm.getProcessDefStringValue(r1, "company")
          r5 <- bpm.getProcessExtendedInfo(r1)
        } yield r4 shouldBe "updated"
      }
    }
    "startProcess" must {
      "complete process and create history data" in {
        val probe = newTestProbe
          .withSubscriptionOnRunnableEvents
          .withSubscriptionOnProcessEvents

        def expectations = for {
          bpm <- newBpm()
          r1 <- bpm.createProcessAndGetId(generateCreateProcessInfo())
          r2 <- bpm.startProcess(r1)
        } yield {
          fishForStartProcess(probe)
          fishForCompleteProcess(probe)
          (bpm, r1)
        }
        for {
          (bpm, processId) <- expectations
          r2 <- bpm.getProcessHistoryData(processId)
        } yield succeed
      }
    }
    "updateProcessJson" must {
      "update process properties" in {
        val json = """
            |{
            |  "value" : {
            |    "company" : {
            |      "value" : "Sphera"
            |    },
            |    "email" : {
            |      "value" : "test@sphera.com"
            |    }
            |  }
            |}
          """.stripMargin

        for {
          bpm <- newBpm()
          r1 <- bpm.createProcessAndGetId(generateCreateProcessInfo())
          r2 <- bpm.updateProcessJson(r1, parse(json))
          r3 <- bpm.getProcessDefStringValue(r1, "company")
          r4 <- bpm.getProcessDefStringValue(r1, "email")
        } yield {
          r3 shouldBe "Sphera"
          r4 shouldBe "test@sphera.com"
        }
      }
    }
  }
}