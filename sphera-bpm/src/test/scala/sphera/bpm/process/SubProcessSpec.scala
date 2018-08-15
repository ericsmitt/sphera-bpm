package sphera.bpm.process

import akka.actor.{ ActorSystem, PoisonPill }
import akka.testkit.{ TestKit, TestProbe }
import sphera.bpm.Bpm
import sphera.bpm.Implicits._

import scala.concurrent.Future

class SubProcessSpec extends TestKit(ActorSystem("SubProcessSpec")) with NewSubProcess {
  "A SubProcess" must {
    "complete" in {
      val probe: TestProbe = newTestProbe
        .withSubscriptionOnRunnableEvents
        .withSubscriptionOnProcessEvents
        .withSubscriptionOnHistoryEvents

      def expectComplete = for {
        bpm <- newBpm()
        r1 <- bpm.createProcessAndGetId(generateCreateProcessInfo(templateId = ProcessTemplates.SubProcess.id))
        r2 <- bpm.startProcess(r1)
      } yield {
        fishForStartProcess(probe)
        fishForActivate(probe, List(subProcessA, subProcessB))
        bpm.submitTaskForm(fishForStartUserTask(probe), enterDataUserTaskData)
        bpm.submitTaskForm(fishForStartUserTask(probe), validationUserTaskData)
        fishForCreateHistoryGetDefStorage(probe, r1).getDefStringValue("company") shouldBe "Ltd Sphera (updated)"
        bpm
      }

      for {
        bpm <- expectComplete
        r1 <- bpm.getProcessInfoMap
        r2 <- bpm.getTaskInfoAll
      } yield {
        r1 shouldBe empty
        r2 shouldBe empty
      }
    }
    "complete after recover" in {
      val bpmId = generateBpmId

      val probe: TestProbe = newTestProbe
        .withSubscriptionOnRunnableEvents
        .withSubscriptionOnProcessEvents
        .withSubscriptionOnHistoryEvents

      def expectComplete = for {
        bpm <- newBpm(bpmId)
        r1 <- bpm.createProcessAndGetId(generateCreateProcessInfo(templateId = ProcessTemplates.SubProcess.id))
        r2 <- bpm.startProcess(r1)
        _ <- bpm.submitTaskForm(fishForStartUserTask(probe), enterDataUserTaskData)
        _ <- waitSome
        r3 <- Future { bpm.bpmActor ! PoisonPill }
        _ <- waitSome
        bpm <- Bpm(bpmId)
      } yield {
        bpm.submitTaskForm(fishForStartUserTask(probe), validationUserTaskData)
        fishForCreateHistoryGetDefStorage(probe, r1).getDefStringValue("company") shouldBe "Ltd Sphera (updated)"
        bpm
      }

      for {
        bpm <- expectComplete
        _ <- waitSome
        _ <- waitSome
        r1 <- bpm.getProcessInfoMap
        r2 <- bpm.getTaskInfoAll
        r3 <- bpm.getProcessHistoryDataMap
        r4 <- bpm.getTaskHistoryDataAll
      } yield {
        r1 shouldBe empty
        r2 shouldBe empty
        r3 should have size 3
        r4 should have size 6
      }
    }
  }
}