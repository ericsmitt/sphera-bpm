package sphera.bpm.task.actor

import sphera.bpm.model.ImplicitModifier
import sphera.bpm.process.Operation
import sphera.bpm.process.actor.ProcessState
import sphera.bpm.runnable.RunnableBehavior
import sphera.bpm.task.TaskTypes

import scala.reflect.ClassTag

trait TaskActor[S <: TaskState] extends RunnableBehavior with TaskTypes with ImplicitModifier {
  type State = S
  val tag: ClassTag[State]
  val ctag7 = tag

  def historyManager = bpm.taskHistoryManager
}
