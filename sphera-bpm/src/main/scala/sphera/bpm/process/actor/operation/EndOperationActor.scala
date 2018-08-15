package sphera.bpm.process.actor.operation

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.lang.Assignments
import sphera.bpm.process.Operation._
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.project.Project
import sphera.bpm.runnable.Runnable.Completed
import sphera.bpm.task.Task
import sphera.core.akkaext.actor.EmptyState

import scala.concurrent.{ ExecutionContext, Future }

class EndOperationActor(
  val id: Operation.Id,
  val bpmnId: Operation.BpmnId,
  val name: Operation.Name,
  val in: List[Operation.BpmnId],
  val assignments: Assignments,
  val projectId: Project.Id,
  val processId: Process.Id,
  val bpm: Bpm,
  val initState: EmptyState = EmptyState())(implicit val c: ExecutionContext, val t: Timeout) extends OperationActor[EmptyState] {
  val out = List.empty

  def activate(state: State, from: Option[Operation.BpmnId]): Unit = {
    evalAssignments(assignments, createContext()) foreach { _ =>
      bpm.getProcessInfo(processId).map(_.parentId) foreach {
        case Some(x) =>
          // must notify SubProcessTask and wait
          bpm.getTaskIdBySubProcessId(processId) foreach {
            case Some(taskId) => bpm.forwardToTask(taskId, Task.CompleteSubProcessCmd)
            case None =>
          }
          log.info("wait")
        case None =>
          bpm.completeProcess(processId)
          log.info("deactivate")
      }
    }
  }
}

object EndOperationActor {
  def props(
    id: Operation.Id,
    bpmnId: Operation.BpmnId,
    name: Operation.Name,
    in: List[Operation.BpmnId],
    assignments: Assignments,
    projectId: Project.Id,
    processId: Process.Id,
    bpm: Bpm)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new EndOperationActor(
      id = id,
      bpmnId = bpmnId,
      name = name,
      in = in,
      assignments = assignments,
      projectId = projectId,
      processId = processId,
      bpm = bpm))
  }
}
