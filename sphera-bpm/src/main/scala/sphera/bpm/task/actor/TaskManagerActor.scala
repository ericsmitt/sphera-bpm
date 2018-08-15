package sphera.bpm.task.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.runnable.{ RunnableManagerBehavior, RunnableManagerState }
import sphera.bpm.task._

import scala.concurrent.ExecutionContext

class TaskManagerActor(
  val id: TaskManager.Id,
  val bpm: Bpm,
  val initState: TaskManagerState)(implicit val c: ExecutionContext, val t: Timeout)
  extends RunnableManagerBehavior with TaskManagerTypes

object TaskManagerActor {
  def props(
    id: TaskManager.Id,
    bpm: Bpm,
    initState: TaskManagerState = TaskManagerState.empty)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new TaskManagerActor(
      id = id,
      bpm: Bpm,
      initState = initState))
  }
}

case class TaskManagerState(storage: Map[Task.RawId, TaskInfo]) extends RunnableManagerState
  with TaskManagerStateTypes

object TaskManagerState {
  def empty = TaskManagerState(Map.empty)
}