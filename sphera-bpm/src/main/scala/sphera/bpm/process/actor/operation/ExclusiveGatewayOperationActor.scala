package sphera.bpm.process.actor.operation

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.bpmn.BpmnId
import sphera.bpm.lang.{ Assignments, Branches, EvaluationContext }
import sphera.bpm.process.Operation._
import sphera.bpm.process.{ CaseBranch, Operation, OtherwiseBranch, Process }
import sphera.bpm.project.Project
import sphera.core.akkaext.actor.EmptyState

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The Exclusive Gateway has pass-through semantics for a set of incoming branches (merging behavior). Further on,
 * each activation leads to the activation of exactly one out of the set of outgoing branches (branching behavior).
 */
class ExclusiveGatewayOperationActor(
  val id: Operation.Id,
  val bpmnId: Operation.BpmnId,
  val name: Operation.Name,
  val in: List[Operation.BpmnId],
  val out: List[Operation.BpmnId],
  val conditions: Branches,
  val assignments: Assignments,
  val projectId: Project.Id,
  val processId: Process.Id,
  val bpm: Bpm,
  val initState: EmptyState = EmptyState())(implicit val c: ExecutionContext, val t: Timeout) extends OperationActor[EmptyState] {

  val ctx = createContext()

  def deactivate(op: Operation.BpmnId): Unit = {
    parent ! Process.ActivateCmd(List(op), Option(bpmnId))
    log.info("deactivate")
  }

  def evalConditions(f1: Future[(Boolean, Operation.BpmnId)], conditions: Branches, ctx: EvaluationContext): Future[(Boolean, Operation.BpmnId)] = {
    f1 flatMap {
      case (false, _) if conditions.isEmpty => sys.error("Inclusive gateway exception")
      case (false, _) =>
        conditions.head match {
          case CaseBranch(desc, expr, op) =>
            val f2 = expr.eval(ctx)
              .map(_.value)
              .map {
                case x: Boolean => x -> op
                case _ => false -> op
              }
            evalConditions(f2, conditions.tail, ctx)
          case OtherwiseBranch(desc, operation) =>
            evalConditions(Future.successful(true -> operation), List.empty, ctx)
        }
      case x @ (true, op) => f1
    }
  }

  def activate(state: State, from: Option[Operation.BpmnId]): Unit = {
    for {
      r1 <- evalAssignments(assignments, ctx)
      (_, op) <- evalConditions(Future.successful(false -> BpmnId("")), conditions, ctx)
    } yield deactivate(op)
  }
}

object ExclusiveGatewayOperationActor {
  def props(
    id: Operation.Id,
    bpmnId: Operation.BpmnId,
    name: Operation.Name,
    in: List[Operation.BpmnId],
    out: List[Operation.BpmnId],
    conditions: Branches,
    assignments: Assignments,
    projectId: Project.Id,
    processId: Process.Id,
    bpm: Bpm)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new ExclusiveGatewayOperationActor(
      id = id,
      bpmnId = bpmnId,
      name = name,
      in = in,
      out = out,
      conditions = conditions,
      assignments = assignments,
      projectId = projectId,
      processId = processId,
      bpm = bpm))
  }
}