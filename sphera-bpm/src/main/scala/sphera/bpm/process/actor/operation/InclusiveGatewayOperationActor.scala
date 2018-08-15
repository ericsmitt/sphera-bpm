package sphera.bpm.process.actor.operation

import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.lang.{ Assignments, Branches }
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.project.Project
import sphera.core.akkaext.actor.CqrsState

import scala.concurrent.ExecutionContext

/**
 * An inclusive gateway breaks the processId flow into one or more flows.
 *
 * A diverging Inclusive Gateway (Inclusive Decision) can be used to create alternative but also parallel paths within a
 * Process flow. Unlike the Exclusive Gateway, all condition Expressions are evaluated. The true evaluation of one
 * condition Expression does not exclude the evaluation of other condition Expressions. All Sequence Flows with
 * a true evaluation will be traversed by a token. Since each p4 is considered to be independent, all combinations of the
 * paths MAY be taken, from zero to all. However, it should be designed so that at least one p4 is taken.
 *
 * A converging Inclusive Gateway is used to merge a combination of alternative and parallel paths. A control flow token
 * arriving at an Inclusive Gateway MAY be synchronized with some other tokens that arrive later at this Gateway.
 */
abstract class InclusiveGatewayOperationActor(
  val id: Operation.Id,
  val bpm: Bpm,
  val bpmnId: Operation.BpmnId,
  val name: Operation.Name,
  val in: List[Operation.BpmnId],
  val out: List[Operation.BpmnId],
  val conditions: Branches,
  val assignments: Assignments,
  val projectId: Project.Id,
  val processId: Process.Id,
  val initState: InclusiveGatewayOperationState)(implicit val c: ExecutionContext, val t: Timeout) extends OperationActor[InclusiveGatewayOperationState] {

  def activate(from: Option[Operation.BpmnId]) = ???
}

// todo: implement
case class InclusiveGatewayOperationState() extends CqrsState {
  def update = ???
}

object InclusiveGatewayOperationState {
  def empty = InclusiveGatewayOperationState()
}

//object InclusiveGatewayOperationActor {
//  def props(
//    id: Operation.Id,
//    bpmnId: Operation.BpmnId,
//    name: Operation.Name,
//    in: List[Operation.BpmnId],
//    out: List[Operation.BpmnId],
//    sequenceflowExpr: List[Branch],
//    assignments: Assignments, projectId: ProjectTypes.Id,
//    processId: Process.Id,
//    getState: InclusiveGatewayOperationState = InclusiveGatewayOperationState.empty)(implicit c: ExecutionContext, t: Timeout) = {
//    Props(new InclusiveGatewayOperationActor(
//      id = id,
//      bpmnId = bpmnId,
//      name = name,
//      in = in,
//      out = out,
//      sequenceflowExpr = sequenceflowExpr,
//      assignments = assignments,
//      projectId = projectId,
//      processId = processId,
//      initState = getState))
//  }
//}
