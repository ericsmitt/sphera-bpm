//package sphera.bpm.serialization
//
//import sphera.bpm.process.actor.ProcessActor._
//import sphera.bpm.process.actor.ProcessManagerActor._
//import sphera.bpm.process.actor.{ ProcessManagerState, ProcessState }
//import sphera.bpm.process.Operation.Operations
//import sphera.bpm.process._
//import sphera.bpm.process.model._
//import sphera.bpm.serializer.proto.process._
//
//trait ProcessConverters extends DefinitionConverters
//  with ModelConverters
//  with LangConverters
//  with ExceptionConverters {
//  import Implicits._
//
//  val ProcessManagerStateManifestV1 = "ProcessManagerState.v1"
//  val CreatedProcessInfoEvtManifestV1 = "CreatedProcessInfoEvt.v1"
//  val UpdatedProcessInfoEvtManifestV1 = "UpdatedProcessInfoEvt.v1"
//  val DeletedProcessInfoEvtManifestV1 = "DeletedProcessInfoEvt.v1"
//
//  val ProcessStateManifestV1 = "ProcessState.v1"
//  val ActivatedEvtManifestV1 = "OperationActivatedEvt.v1"
//
//  def toProcessManagerStateBinary(obj: ProcessManagerState): Array[Byte] = {
//    val x = obj.storage map { case (a, b) => a -> toProcessInfo(b) }
//    ProcessManagerStateV1(x).toByteArray
//  }
//
//  def fromProcessRuntimeState(x: ProcessManagerStateV1): ProcessManagerState = {
//    val y = x.v map { case (a, b) => a -> fromProcessInfo(b) }
//    ProcessManagerState(y)
//  }
//
//  def fromProcessManagerStateBinary(bytes: Array[Byte]): ProcessManagerState = {
//    val x = ProcessManagerStateV1.parseFrom(bytes).v.map { case (a, b) => (a, fromProcessInfo(b)) }
//    ProcessManagerState(x)
//  }
//
//  def toCreatedProcessInfoEvtBinary(obj: CreatedProcessInfoEvt): Array[Byte] = {
//    CreatedProcessInfoEvtV1(toProcessInfo(obj.x)).toByteArray
//  }
//
//  def toUpdatedProcessInfoEvtBinary(obj: UpdatedProcessInfoEvt): Array[Byte] = {
//    UpdatedProcessInfoEvtV1(toUpdateProcessInfo(obj.x), obj.modifier).toByteArray
//  }
//
//  def toDeletedProcessInfoEvtBinary(obj: DeletedProcessInfoEvt): Array[Byte] = {
//    DeletedProcessInfoEvtV1(processId = obj.processId).toByteArray
//  }
//
//  def fromUpdatedProcessInfoEvtBinary(bytes: Array[Byte]): UpdatedProcessInfoEvt = {
//    val x = UpdatedProcessInfoEvtV1.parseFrom(bytes)
//    UpdatedProcessInfoEvt(fromUpdateProcessInfo(x.x), x.modifier)
//  }
//
//  def fromCreatedProcessInfoEvtBinary(bytes: Array[Byte]): CreatedProcessInfoEvt = {
//    val x = CreatedProcessInfoEvtV1.parseFrom(bytes).x
//    CreatedProcessInfoEvt(fromProcessInfo(x))
//  }
//
//  def toUpdateProcessInfo(x: UpdateProcessInfo): UpdateProcessInfoV1 = {
//    UpdateProcessInfoV1(
//      id = x.id,
//      name = x.name,
//      description = x.description.flatten,
//      status = x.status.map(toProcessStatus),
//      managerId = x.managerId,
//      startedBy = x.startedBy.map(x => x.map(toModifier)).getOrElse(None),
//      startedOn = x.startedOn.map(zonedDateTimeOptToStringOpt).getOrElse(None))
//  }
//
//  def fromUpdateProcessInfo(x: UpdateProcessInfoV1): UpdateProcessInfo = {
//    UpdateProcessInfo(
//      id = x.id,
//      name = x.name,
//      description = x.description,
//      status = x.status.map(fromProcessStatus),
//      managerId = x.managerId,
//      startedBy = Option(x.startedBy.map(fromModifier)),
//      startedOn = Option(x.startedOn))
//  }
//
//  def fromDeletedProcessInfoEvtBinary(bytes: Array[Byte]): DeletedProcessInfoEvt = {
//    val x = DeletedProcessInfoEvtV1.parseFrom(bytes).processId
//    DeletedProcessInfoEvt(processId = x)
//  }
//
//  def toProcessStateBinary(obj: ProcessState): Array[Byte] = {
//    toProcessState(obj).toByteArray
//  }
//
//  implicit def toProcessState(x: ProcessState): ProcessStateV1 = {
//    ProcessStateV1(
//      processDefinition = x.processDef,
//      roleDefinitions = x.roleDef,
//      definitions = x.defStorage,
//      activeOperations = x.activeOperations,
//      exceptions = x.exceptions.map(toBpmException))
//  }
//
//  def fromProcessStateBinary(bytes: Array[Byte]): ProcessState = {
//    ProcessStateV1.parseFrom(bytes)
//  }
//
//  implicit def fromProcessState(x: ProcessStateV1): ProcessState = {
//    ProcessState(
//      processDef = x.processDefinition,
//      roleDef = x.roleDefinitions,
//      defStorage = x.definitions,
//      activeOperations = x.activeOperations,
//      exceptions = x.exceptions.map(fromBpmException))
//  }
//
//  def toActivatedEvtBinary(x: ActivatedEvt): Array[Byte] = {
//    toActivatedEvt(x).toByteArray
//  }
//
//  def fromActivatedEvtBinary(x: Array[Byte]): ActivatedEvt = {
//    fromActivatedEvt(ActivatedEvtV1.parseFrom(x))
//  }
//
//  def toActivatedEvt(x: ActivatedEvt): ActivatedEvtV1 = {
//    ActivatedEvtV1(
//      to = x.to,
//      from = x.from)
//  }
//
//  def fromActivatedEvt(x: ActivatedEvtV1): ActivatedEvt = {
//    ActivatedEvt(
//      to = x.to,
//      from = x.from)
//  }
//
//  implicit def toProcessDefinition(x: ProcessDef): ProcessDefinitionV1 = {
//    ProcessDefinitionV1(
//      roleDefinitions = x.roleDef,
//      definitions = x.defStorage,
//      operations = x.opDef)
//  }
//
//  implicit def fromProcessDefinition(x: ProcessDefinitionV1): ProcessDef = {
//    ProcessDefinition(
//      roleDefinitions = x.roleDefinitions,
//      defStorage = x.definitions,
//      operations = x.operations)
//  }
//
//  implicit def toOperation(x: Operation): OperationV1 = {
//    x match {
//      case y: StartOperationDef =>
//        OperationV1.defaultInstance.withOpt1(
//          StartOperationV1(
//            bpmId = y.bpmnId,
//            name = y.name,
//            outgoings = y.out))
//      case y: EndOperationDef =>
//        OperationV1.defaultInstance.withOpt2(
//          EndOperationV1(
//            bpmId = y.bpmnId,
//            name = y.name))
//      case y: ParallelGatewayOperationDef =>
//        OperationV1.defaultInstance.withOpt3(
//          ParallelGatewayOperationV1(
//            bpmId = y.bpmnId,
//            name = y.name,
//            incomings = y.incomings,
//            outgoings = y.outgoings))
//      case y: ExclusiveGatewayOperationDef =>
//        OperationV1.defaultInstance.withOpt4(
//          ExclusiveGatewayOperationV1(
//            bpmId = y.bpmnId,
//            name = y.name,
//            conditions = y.conditions.map(toBranch)))
//      case y: ScriptTaskOperationDef =>
//        OperationV1.defaultInstance.withOpt5(
//          ScriptTaskOperationV1(
//            bpmId = y.bpmnId,
//            name = y.name,
//            assignments = toAssignments(y.assignments),
//            outgoing = y.outgoing))
//      case y: SendTaskOperationDef =>
//        OperationV1.defaultInstance.withOpt6(
//          MailOperationV1(
//            bpmId = y.bpmnId,
//            name = y.name,
//            outgoing = y.outgoing,
//            addresses = toExpressions(y.addresses),
//            subject = toExpression(y.subject),
//            message = toExpression(y.message)))
//      case y: UserTaskOperationDef =>
//        OperationV1.defaultInstance.withOpt7(
//          UserTaskOperationV1(
//            bpmId = y.bpmnId,
//            name = y.name,
//            role = y.role,
//            ownerId = y.ownerId,
//            assigneeId = y.assigneeId,
//            reviewerId = y.reviewerId,
//            watcherIds = y.watcherIds,
//            nextOperationBpmId = y.nextOperationBpmId,
//            formTemplateId = y.formTemplateId,
//            plannedStart = y.plannedStart,
//            plannedDuration = y.plannedDuration,
//            importAssignments = y.importAssignments,
//            exportAssignments = y.exportAssignments))
//    }
//  }
//
//  implicit def toOperations(x: Operations): Seq[OperationV1] = {
//    x.map(toOperation)
//  }
//
//  implicit def fromOperation(x: OperationV1): Operation = {
//    val opt1 = x.operationOneof.opt1
//    val opt2 = x.operationOneof.opt2
//    val opt3 = x.operationOneof.opt3
//    val opt4 = x.operationOneof.opt4
//    val opt5 = x.operationOneof.opt5
//    val opt6 = x.operationOneof.opt6
//    val opt7 = x.operationOneof.opt7
//    Seq(opt1, opt2, opt3, opt4, opt5, opt6, opt7).flatten.head match {
//      case y: StartOperationV1 =>
//        StartOperation(
//          bpmnId = y.bpmId,
//          name = y.name,
//          outgoings = y.outgoings)
//      case y: EndOperationV1 =>
//        EndOperation(
//          bpmnId = y.bpmId,
//          name = y.name)
//      case y: ParallelGatewayOperationV1 =>
//        ParallelGatewayOperation(
//          bpmnId = y.bpmId,
//          name = y.name,
//          incomings = y.incomings,
//          outgoings = y.outgoings)
//      case y: ExclusiveGatewayOperationV1 =>
//        ExclusiveGatewayOperation(
//          bpmnId = y.bpmId,
//          name = y.name,
//          conditions = fromBranches(y.conditions))
//      case y: ScriptTaskOperationV1 =>
//        ScriptTaskOperation(
//          bpmnId = y.bpmId,
//          name = y.name,
//          outgoing = y.outgoing,
//          assignments = fromAssignments(y.assignments))
//      case y: MailOperationV1 =>
//        MailOperation(
//          bpmnId = y.bpmId,
//          name = y.name,
//          outgoing = y.outgoing,
//          addresses = fromExpressions(y.addresses),
//          subject = fromExpression(y.subject),
//          message = fromExpression(y.message))
//      case y: UserTaskOperationV1 =>
//        UserTaskOperation(
//          bpmnId = y.bpmId,
//          name = y.name,
//          role = y.role,
//          ownerId = y.ownerId,
//          assigneeId = y.assigneeId,
//          reviewerId = y.reviewerId,
//          watcherIds = y.watcherIds,
//          nextOperationBpmId = y.nextOperationBpmId,
//          formTemplateId = y.formTemplateId,
//          plannedStart = y.plannedStart,
//          plannedDuration = y.plannedDuration,
//          importAssignments = y.importAssignments,
//          exportAssignments = y.exportAssignments)
//    }
//  }
//
//  implicit def fromOperations(x: Seq[OperationV1]): Operations = {
//    x.map(fromOperation).toList
//  }
//
//  def toBranch(x: Branch): BranchV1 = {
//    x match {
//      case y: CaseBranch =>
//        BranchV1.defaultInstance.withOpt1(
//          CaseBranchV1(
//            desc = y.desc,
//            expression = toExpression(y.expr),
//            operation = y.operation))
//      case y: OtherwiseBranch =>
//        BranchV1.defaultInstance.withOpt2(
//          OtherwiseBranchV1(
//            desc = y.desc,
//            operation = y.operation))
//    }
//  }
//
//  def fromBranch(x: BranchV1): Branch = {
//    val opt1 = x.branchOneof.opt1
//    val opt2 = x.branchOneof.opt2
//    Seq(opt1, opt2).flatten.head match {
//      case y: CaseBranchV1 =>
//        CaseBranch(
//          desc = y.desc,
//          expr = fromExpression(y.expression),
//          operation = y.operation)
//      case y: OtherwiseBranchV1 =>
//        OtherwiseBranch(
//          desc = y.desc,
//          operation = y.operation)
//    }
//  }
//
//  def fromBranches(x: Seq[BranchV1]): List[Branch] = {
//    x.map(fromBranch).toList
//  }
//
//  implicit def toProcessInfo(x: ProcessInfo): ProcessInfoV1 = {
//    ProcessInfoV1(
//      id = x.id,
//      name = x.name,
//      description = x.description,
//      status = x.status,
//      bpmId = x.bpmnId,
//      templateId = x.templateId,
//      managerId = x.managerId,
//      startedBy = x.startedBy.map(toModifier),
//      startedOn = x.startedOn,
//      modifyAttr = toModifyAttr(x.modifyAttr))
//  }
//
//  implicit def fromProcessInfo(x: ProcessInfoV1): ProcessInfo = {
//    ProcessInfo(
//      id = x.id,
//      name = x.name,
//      description = x.description,
//      status = x.status,
//      bpmnId = x.bpmId,
//      templateId = x.templateId,
//      managerId = x.managerId,
//      startedBy = x.startedBy.map(fromModifier),
//      startedOn = x.startedOn,
//      modifyAttr = fromModifyAttr(x.modifyAttr))
//  }
//
//  implicit def toProcessStatus(x: RunnableStatus): Int = {
//    x match {
//      case RunnableStatus.Created => 1
//      case RunnableStatus.Started => 2
//      case RunnableStatus.Completed => 3
//      case RunnableStatus.Deleted => 4
//      case RunnableStatus.Failed => 5
//      case _ => 1
//    }
//  }
//
//  implicit def fromProcessStatus(x: Int): RunnableStatus = {
//    x match {
//      case 1 => RunnableStatus.Created
//      case 2 => RunnableStatus.Started
//      case 3 => RunnableStatus.Completed
//      case 4 => RunnableStatus.Deleted
//      case 5 => RunnableStatus.Failed
//      case _ => RunnableStatus.Created
//    }
//  }
//
//  implicit def toProcessExtendedInfo(x: ProcessExtendedInfo): ProcessExtendedInfoV1 = {
//    ProcessExtendedInfoV1(
//      info = x.info,
//      state = x.state)
//  }
//
//  implicit def fromProcessExtendedInfo(x: ProcessExtendedInfoV1): ProcessExtendedInfo = {
//    ProcessExtendedInfo(
//      info = x.info,
//      state = x.state)
//  }
//}