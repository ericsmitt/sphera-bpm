package sphera.bpm.process.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.process._
import sphera.bpm.runnable.{ RunnableManagerBehavior, RunnableManagerState }

import scala.concurrent.ExecutionContext

class ProcessManagerActor(
  val id: ProcessManager.Id,
  val bpm: Bpm,
  val initState: ProcessManagerState)(implicit val c: ExecutionContext, val t: Timeout)
  extends RunnableManagerBehavior with ProcessManagerTypes

object ProcessManagerActor {
  def props(id: ProcessManager.Id, bpm: Bpm, state: ProcessManagerState = ProcessManagerState.empty)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new ProcessManagerActor(id = id, bpm = bpm, initState = state))
  }
}

case class ProcessManagerState(storage: Map[Process.RawId, ProcessInfo]) extends RunnableManagerState
  with ProcessManagerStateTypes

object ProcessManagerState {
  def empty = ProcessManagerState(Map.empty)
}