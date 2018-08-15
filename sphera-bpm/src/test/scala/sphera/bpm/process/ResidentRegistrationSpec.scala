package sphera.bpm.process

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import sphera.bpm.Bpm
import sphera.bpm.Implicits._
import sphera.bpm.task.Task

import scala.concurrent.Future

/**
 * = Бизнес процесс регистрации резидента =
 *
 * Описан в файле resources/bpmn/ResidentRegistration.xml
 */
class ResidentRegistrationSpec extends TestKit(ActorSystem("ResidentRegistrationSpec")) with NewSubProcess {
  "A ResidentRegistration" must {
    "complete" in {
      val probe: TestProbe = newTestProbe
        .withSubscriptionOnRunnableEvents
        .withSubscriptionOnProcessEvents

      def expectEnterDataUserTask: Future[(Bpm, Process.Id, Task.Id)] = for {
        bpm <- newBpm()
        r1 <- bpm.createProcessAndGetId(generateCreateProcessInfo(templateId = ProcessTemplates.ResidentRegistration.id))
        r2 <- bpm.startProcess(r1)
      } yield {
        fishForStartProcess(probe)
        fishForActivate(probe, enterDataOperationId)
        (bpm, r1, fishForStartUserTask(probe))
      }

      def expectValidationUserTask: Future[(Bpm, Process.Id, Task.Id)] = for {
        (bpm, processId, taskId) <- expectEnterDataUserTask
        r1 <- bpm.submitTaskForm(taskId, enterDataUserTaskData)
      } yield (bpm, processId, fishForStartUserTask(probe))

      for {
        (bpm, processId, taskId) <- expectValidationUserTask
        r1 <- bpm.submitTaskForm(taskId, validationUserTaskData)
        r2 <- bpm.getProcessExtendedInfo(processId)
      } yield {
        fishForCompleteProcess(probe)

        info("===================== Result defStorage =====================")
        info(r2.state.defStorage)

        info("===================== Schema =====================")
        info(r2.state.defStorage.schema.get)

        succeed
      }
    }
  }
}