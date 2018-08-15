package sphera.bpm.process

import akka.testkit.{ TestKit, TestProbe }
import sphera.bpm.Implicits._
import sphera.bpm.process.Process.ActivatedEvt
import sphera.bpm.runnable.{ RunnableManager, RunnableStatus }
import sphera.bpm.runnable.RunnableManager._
import sphera.core.test.PersistenceSpec
import org.scalatest.Assertion

import scala.concurrent.duration._
import sphera.bpm.task.{ Task, TaskInfo, TaskType, UpdateTaskInfo }

trait TaskExpectations extends RunnableExpectations { _: PersistenceSpec with TestKit =>
  def fishForCreateUserTask(p: TestProbe): Task.Id = {
    val event = p.fishForSpecificMessage[CreatedRunnableInfoEvt](2 second, s"fishForCreateUserTask") {
      case event @ CreatedRunnableInfoEvt(info: TaskInfo) if info.taskType == TaskType.UserTask => event
    }
    val taskId = event.info.id
    info(s"UserTask created [${taskId.name}]")
    taskId
  }

  def fishForCreateSendTask(p: TestProbe): Task.Id = {
    val event = p.fishForSpecificMessage[CreatedRunnableInfoEvt](2 second, s"fishForCreateSendTask") {
      case event @ CreatedRunnableInfoEvt(info: TaskInfo) if info.taskType == TaskType.SendTask => event
    }
    val taskId = event.info.id
    info(s"SendTask created [${taskId.name}]")
    taskId
  }

  def fishForStartUserTask(p: TestProbe): Task.Id = {
    val taskId = fishForCreateUserTask(p)
    val event = p.fishForSpecificMessage[UpdatedRunnableInfoEvt](2 second, s"fishForStartTask") {
      case event @ UpdatedRunnableInfoEvt(update: UpdateTaskInfo, _) if (update.id == taskId) && (update.status contains RunnableStatus.Started) => event
    }
    info(s"UserTask started [${taskId.name}]")
    taskId
  }

  def fishForStartSendTask(p: TestProbe): Task.Id = {
    val taskId = fishForCreateSendTask(p)
    val event = p.fishForSpecificMessage[UpdatedRunnableInfoEvt](2 second, s"fishForSendTask") {
      case event @ UpdatedRunnableInfoEvt(update: UpdateTaskInfo, _) if (update.id == taskId) && (update.status contains RunnableStatus.Started) => event
    }
    info(s"SendTask started [${taskId.name}]")
    taskId
  }
}
