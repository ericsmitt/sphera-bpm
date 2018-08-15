package sphera.bpm.task.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.process.Process
import sphera.bpm.storage.runnable.RunnableStorage
import sphera.bpm.task.Task
import sphera.bpm.task.Task.{ CompleteSubProcessCmd, Done }

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class SubProcessTaskActor(
  val id: Task.Id,
  val processId: Process.Id,
  val subProcessId: Process.Id,
  val bpm: Bpm,
  val initState: SubProcessTaskState)(implicit val c: ExecutionContext, val t: Timeout, val tag: ClassTag[SubProcessTaskState])
  extends TaskActor[SubProcessTaskState] {

  def start(state: SubProcessTaskState): Unit = {
    // must not start if already started
    bpm.isProcessStarted(subProcessId) foreach {
      case true =>
      case false => bpm.startProcess(subProcessId)
    }
  }

  def completeSubProcess(state: SubProcessTaskState): Unit = {
    bpm.completeTask(id)
    sender() ! Done
  }

  def behavior(state: SubProcessTaskState) = {
    case CompleteSubProcessCmd => completeSubProcess(state)
  }
}

case class SubProcessTaskState(storage: RunnableStorage) extends TaskState {
  def update: Update = PartialFunction.empty
}

object SubProcessTaskState {
  def empty = SubProcessTaskState(RunnableStorage.empty)
}

object SubProcessTaskActor {
  def props(
    taskId: Task.Id,
    processId: Process.Id,
    subProcessId: Process.Id,
    bpm: Bpm,
    state: SubProcessTaskState = SubProcessTaskState.empty)(implicit c: ExecutionContext, t: Timeout): Props = {
    Props(new SubProcessTaskActor(
      id = taskId,
      processId = processId,
      subProcessId = subProcessId,
      bpm = bpm,
      initState = state))
  }
}