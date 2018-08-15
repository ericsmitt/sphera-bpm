package sphera.bpm.process.actor.operation

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.lang.Assignments
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.project.Project
import sphera.core.akkaext.actor.EmptyState

import scala.concurrent.ExecutionContext

class StartOperationActor(
  val id: Operation.Id,
  val bpmnId: Operation.BpmnId,
  val name: Operation.Name,
  val out: List[Operation.BpmnId],
  val assignments: Assignments,
  val projectId: Project.Id,
  val processId: Process.Id,
  val bpm: Bpm,
  val initState: EmptyState = EmptyState())(implicit val c: ExecutionContext, val t: Timeout) extends OperationActor[EmptyState] {
  val in = List.empty

  def activate(state: State, from: Option[Operation.BpmnId]): Unit = {
    evalAssignments(assignments, createContext()).map(_ => deactivate())
  }
}

object StartOperationActor {
  def props(
    id: Operation.Id,
    bpmnId: Operation.BpmnId,
    name: Operation.Name,
    out: List[Operation.BpmnId],
    assignments: Assignments,
    projectId: Project.Id,
    processId: Process.Id,
    bpm: Bpm)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new StartOperationActor(
      id = id,
      bpmnId = bpmnId,
      name = name,
      out = out,
      assignments = assignments,
      projectId = projectId,
      processId = processId,
      bpm = bpm))
  }
}
