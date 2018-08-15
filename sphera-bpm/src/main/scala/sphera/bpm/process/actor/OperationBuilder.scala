package sphera.bpm.process.actor

import sphera.bpm.process._
import sphera.bpm.process.actor.operation._
import sphera.core.akkaext.actor.ActorId

trait OperationBuilder { _: ProcessActor =>
  def buildOperations(opDefs: Set[OperationDef]): Set[Operation.Id] = opDefs map buildOperation

  def buildOperation(opDef: OperationDef) = {
    val operationId = id / opDef.bpmnId.raw
    val (props, x) = opDef match {
      case x: StartOperationDef =>
        StartOperationActor.props(
          id = operationId,
          bpm = bpm,
          bpmnId = x.bpmnId,
          name = x.name,
          out = x.out,
          assignments = x.assignments,
          projectId = projectId,
          processId = id) -> x
      case x: ParallelGatewayOperationDef =>
        ParallelGatewayOperationActor.props(
          id = operationId,
          bpm = bpm,
          bpmnId = x.bpmnId,
          name = x.name,
          in = x.in,
          out = x.out,
          assignments = x.assignments,
          projectId = projectId,
          processId = id) -> x
      case x: ScriptTaskOperationDef =>
        ScriptTaskOperationActor.props(
          id = operationId,
          bpm = bpm,
          bpmnId = x.bpmnId,
          name = x.name,
          loopCharacteristics = LoopCharacteristics.MultiInstanceParallel,
          dataStructureId = None,
          plannedStart = None,
          plannedDuration = None,
          in = x.in,
          out = x.out,
          javascriptCode = None,
          additionalScopeMappings = Map.empty,
          preAssignments = x.preAssignments,
          postAssignments = x.postAssignments,
          projectId = projectId,
          processId = id) -> x
      case x: SendTaskOperationDef =>
        SendTaskOperationActor.props(
          id = operationId,
          bpmnId = x.bpmnId,
          bpm = bpm,
          name = x.name,
          loopCharacteristics = LoopCharacteristics.MultiInstanceParallel,
          dataStructureId = None,
          plannedStart = None,
          plannedDuration = None,
          in = x.in,
          out = x.out,
          recipients = x.recipients,
          subject = x.subject,
          message = x.message,
          preAssignments = x.preAssignments,
          postAssignments = x.postAssignments,
          projectId = projectId,
          processId = id) -> x
      case x: ExclusiveGatewayOperationDef =>
        ExclusiveGatewayOperationActor.props(
          id = operationId,
          bpm = bpm,
          bpmnId = x.bpmnId,
          name = x.name,
          in = x.in,
          out = x.out,
          conditions = x.conditions,
          assignments = x.assignments,
          projectId = projectId,
          processId = id) -> x
      case x: UserTaskOperationDef =>
        UserTaskOperationActor.props(
          id = operationId,
          bpmnId = x.bpmnId,
          name = x.name,
          rolePath = x.role,
          assigneeId = None,
          reviewerId = x.reviewerId,
          watcherIds = x.watcherIds,
          loopCharacteristics = LoopCharacteristics.MultiInstanceParallel,
          formTemplateId = x.formTemplateId,
          dataStructureId = x.dataStructureId,
          plannedStart = x.plannedStart,
          plannedDuration = x.plannedDuration,
          projectId = projectId,
          processId = id,
          preAssignments = x.preAssignments,
          postAssignments = x.postAssignments,
          in = x.in,
          out = x.out,
          bpm = bpm) -> x
      case x: EndOperationDef =>
        EndOperationActor.props(
          id = operationId,
          bpm = bpm,
          bpmnId = x.bpmnId,
          name = x.name,
          in = x.in,
          assignments = x.assignments,
          projectId = projectId,
          processId = id) -> x
      case x: SubProcessOperationDef =>
        SubProcessOperationActor.props(
          id = operationId,
          bpmnId = x.bpmnId,
          name = x.name,
          data = x.data,
          templateId = x.templateId,
          loopCharacteristics = LoopCharacteristics.MultiInstanceParallel,
          taskDataStructureId = x.taskDataStructureId,
          plannedStart = x.plannedStart,
          plannedDuration = x.plannedDuration,
          projectId = projectId,
          processId = id,
          preAssignments = x.preAssignments,
          postAssignments = x.postAssignments,
          in = x.in,
          out = x.out,
          bpm = bpm) -> x
    }

    context.actorOf(props = props, name = x.bpmnId.raw)

    operationId
  }
}