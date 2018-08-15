package sphera.bpm.task.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.storage.runnable.RunnableStorage
import sphera.bpm.task.Task

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class ScriptTaskActor(
  val id: Task.Id,
  val bpm: Bpm,
  val initState: ScriptTaskState)(implicit val c: ExecutionContext, val t: Timeout, val tag: ClassTag[ScriptTaskState])
  extends TaskActor[ScriptTaskState] {

  def start(state: ScriptTaskState): Unit = {
    // todo: Run javascript
    bpm.completeTask(id)
  }

  def behavior(state: State): Receive = PartialFunction.empty
}

case class ScriptTaskState(storage: RunnableStorage) extends TaskState {
  def update: Update = PartialFunction.empty
}

object ScriptTaskState {
  def empty = ScriptTaskState(RunnableStorage.empty)
}

object ScriptTaskActor {
  def props(taskId: Task.Id, bpm: Bpm, state: ScriptTaskState = ScriptTaskState.empty)(implicit c: ExecutionContext, t: Timeout): Props = {
    Props(new ScriptTaskActor(id = taskId, bpm = bpm, initState = state))
  }
}
