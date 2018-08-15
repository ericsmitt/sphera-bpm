//package sphera.bpm.serialization
//
//import sphera.bpm.serializer.proto.task._
//import sphera.bpm.task
//import sphera.bpm.task._
//import sphera.bpm.task.actor.TaskManagerActor.{ CreatedTaskInfoEvt, DeletedTaskInfoEvt, UpdatedTaskInfoEvt }
//import sphera.bpm.task.actor.{ TaskManagerState, TaskState, UserTaskState }
//import sphera.bpm.task.model._
//
//trait TaskConverters extends ModelConverters
//  with DefinitionConverters
//  with ExceptionConverters {
//  import Implicits._
//
//  val TaskManagerStateManifestV1 = "TaskManagerState.v1"
//  val CreatedTaskInfoEvtManifestV1 = "CreatedTaskInfoEvt.v1"
//  val UpdatedTaskInfoEvtManifestV1 = "UpdatedTaskInfoEvt.v1"
//  val DeletedTaskInfoEvtManifestV1 = "DeletedTaskInfoEvt.v1"
//
//  val UserTaskStateManifestV1 = "UserTaskState.v1"
//
//  def toTaskManagerStateBinary(obj: TaskManagerState): Array[Byte] = {
//    val x = obj.v map { case (a, b) => a -> toTaskInfo(b) }
//    TaskManagerStateV1(x).toByteArray
//  }
//
//  def fromTaskManagerState(x: TaskManagerStateV1): TaskManagerState = {
//    val y = x.v map { case (a, b) => a -> fromTaskInfo(b) }
//    TaskManagerState(y)
//  }
//
//  def fromTaskManagerStateBinary(bytes: Array[Byte]): TaskManagerState = {
//    val x = TaskManagerStateV1.parseFrom(bytes).v.map { case (a, b) => (a, fromTaskInfo(b)) }
//    TaskManagerState(x)
//  }
//
//  def fromUserTaskStateBinary(bytes: Array[Byte]): UserTaskState = {
//    val x = UserTaskStateV1.parseFrom(bytes)
//    x
//  }
//
//  def toUserTaskStateBinary(obj: UserTaskState): Array[Byte] = {
//    toUserTaskState(obj).toByteArray
//  }
//
//  implicit def toUserTaskState(x: UserTaskState): UserTaskStateV1 = {
//    UserTaskStateV1(
//      definitions = x.defStorage,
//      exceptions = x.exceptions.map(toBpmException))
//  }
//
//  implicit def fromUserTaskState(x: UserTaskStateV1): UserTaskState = {
//    val x = UserTaskStateV1.defaultInstance.definitions
//    UserTaskState(fromDefinitions(x))
//  }
//
//  def toCreatedTaskInfoEvtBinary(obj: CreatedTaskInfoEvt): Array[Byte] = {
//    CreatedTaskInfoEvtV1(toTaskInfo(obj.x)).toByteArray
//  }
//
//  def toUpdatedTaskInfoEvtBinary(obj: UpdatedTaskInfoEvt): Array[Byte] = {
//    UpdatedTaskInfoEvtV1(toUpdateTaskInfo(obj.x), obj.modifier).toByteArray
//  }
//
//  def toDeletedTaskInfoEvtBinary(obj: DeletedTaskInfoEvt): Array[Byte] = {
//    DeletedTaskInfoEvtV1(taskId = obj.taskId).toByteArray
//  }
//
//  def fromUpdatedTaskInfoEvtBinary(bytes: Array[Byte]): UpdatedTaskInfoEvt = {
//    val x = UpdatedTaskInfoEvtV1.parseFrom(bytes)
//    UpdatedTaskInfoEvt(fromUpdateTaskInfo(x.x), x.modifier)
//  }
//
//  def fromCreatedTaskInfoEvtBinary(bytes: Array[Byte]): CreatedTaskInfoEvt = {
//    val x = CreatedTaskInfoEvtV1.parseFrom(bytes).x
//    CreatedTaskInfoEvt(fromTaskInfo(x))
//  }
//
//  def fromDeletedTaskInfoEvtBinary(bytes: Array[Byte]): DeletedTaskInfoEvt = {
//    val x = DeletedTaskInfoEvtV1.parseFrom(bytes).taskId
//    DeletedTaskInfoEvt(taskId = x)
//  }
//
//  implicit def toTaskInfo(x: TaskInfo): TaskInfoV1 = {
//    TaskInfoV1(
//      id = x.id,
//      name = x.name,
//      description = x.description,
//      taskType = x.taskType,
//      taskStatus = x.status,
//      taskInitiator = x.initiator,
//      ownerId = x.ownerId,
//      assigneeId = x.assigneeId,
//      reviewerId = x.reviewerId,
//      watcherIds = x.watcherIds,
//      processId = x.processId,
//      operationId = x.operationId,
//      operationBpmId = x.operationBpmId,
//      formTemplateId = x.formTemplateId,
//      actualStart = x.actualStart,
//      actualEnd = x.actualEnd,
//      plannedStart = x.plannedStart,
//      plannedDuration = x.plannedDuration,
//      modifyAttr = x.modifyAttr)
//  }
//
//  def toUpdateTaskInfo(x: UpdateTaskInfo): UpdateTaskInfoV1 = {
//    UpdateTaskInfoV1(
//      id = x.id,
//      name = x.name,
//      description = x.description.flatten,
//      taskStatus = x.status.map(toTaskStatus),
//      taskInitiator = x.initiator.map(toTaskInitiator),
//      assigneeId = x.assigneeId.flatMap(UUIDOptToStringOpt),
//      reviewerId = x.reviewerId.flatMap(UUIDOptToStringOpt),
//      watcherIds = x.watcherIds.getOrElse(Set.empty).map(_.toString).toSeq,
//      actualStart = x.actualStart.map(zonedDateTimeOptToStringOpt).getOrElse(None),
//      actualEnd = x.actualEnd.map(zonedDateTimeOptToStringOpt).getOrElse(None))
//  }
//
//  def fromUpdateTaskInfo(x: UpdateTaskInfoV1): UpdateTaskInfo = {
//    task.UpdateTaskInfo(
//      id = x.id,
//      name = x.name,
//      description = x.description,
//      status = x.taskStatus.map(fromTaskStatus),
//      initiator = x.taskInitiator.map(fromTaskInitiator),
//      assigneeId = Option(x.assigneeId),
//      reviewerId = Option(x.reviewerId),
//      watcherIds = Option(x.watcherIds),
//      actualStart = Option(x.actualStart),
//      actualEnd = Option(x.actualEnd))
//  }
//
//  implicit def fromTaskInfo(x: TaskInfoV1): TaskInfo = {
//    TaskInfo(
//      id = x.id,
//      name = x.name,
//      description = x.description,
//      taskType = x.taskType,
//      status = x.taskStatus,
//      initiator = x.taskInitiator,
//      ownerId = x.ownerId,
//      assigneeId = x.assigneeId,
//      reviewerId = x.reviewerId,
//      watcherIds = x.watcherIds,
//      processId = x.processId,
//      operationId = x.operationId,
//      operationBpmId = x.operationBpmId,
//      formTemplateId = x.formTemplateId,
//      actualStart = x.actualStart,
//      actualEnd = x.actualEnd,
//      plannedStart = x.plannedStart,
//      plannedDuration = x.plannedDuration,
//      modifyAttr = x.modifyAttr)
//  }
//
//  implicit def toTaskType(x: TaskType): Int = {
//    x match {
//      case TaskType.UserTask => 1
//      case TaskType.StartProcessTask => 2
//      case TaskType.StartProjectTask => 3
//      case TaskType.ReceiveTask => 4
//      case TaskType.SendTask => 5
//      case TaskType.StartProjectProcessTask => 6
//      case _ => 1
//    }
//  }
//
//  implicit def fromTaskType(x: Int): TaskType = {
//    x match {
//      case 1 => TaskType.UserTask
//      case 2 => TaskType.StartProcessTask
//      case 3 => TaskType.StartProjectTask
//      case 4 => TaskType.ReceiveTask
//      case 5 => TaskType.SendTask
//      case 6 => TaskType.StartProjectProcessTask
//      case _ => TaskType.UserTask
//    }
//  }
//
//  implicit def toTaskStatus(x: TaskStatus): Int = {
//    x match {
//      case TaskStatus.Created => 1
//      case TaskStatus.Started => 2
//      case TaskStatus.Completed => 3
//      case TaskStatus.Deleted => 4
//      case TaskStatus.Failed => 5
//      case _ => 1
//    }
//  }
//
//  implicit def fromTaskStatus(x: Int): TaskStatus = {
//    x match {
//      case 1 => TaskStatus.Created
//      case 2 => TaskStatus.Started
//      case 3 => TaskStatus.Completed
//      case 4 => TaskStatus.Deleted
//      case 5 => TaskStatus.Failed
//      case _ => TaskStatus.Created
//    }
//  }
//
//  implicit def toTaskInitiator(x: Initiator): Int = {
//    x match {
//      case Initiator.ProcessManager => 1
//      case Initiator.ProjectManager => 2
//      case _ => 1
//    }
//  }
//
//  implicit def fromTaskInitiator(x: Int): Initiator = {
//    x match {
//      case 1 => Initiator.ProcessManager
//      case 2 => Initiator.ProjectManager
//      case _ => Initiator.ProcessManager
//    }
//  }
//
//  implicit def toTaskExtendedInfo(x: TaskExtendedInfo): TaskExtendedInfoV1 = {
//    TaskExtendedInfoV1(
//      info = x.info,
//      state = x.state)
//  }
//
//  implicit def toTaskState(x: TaskState): TaskStateV1 = {
//    x match {
//      case y: UserTaskState =>
//        TaskStateV1.defaultInstance.withOpt1(y)
//    }
//  }
//
//  implicit def fromTaskExtendedInfo(x: TaskExtendedInfoV1): TaskExtendedInfo = {
//    TaskExtendedInfo(
//      info = x.info,
//      state = x.state)
//  }
//
//  implicit def fromTaskState(x: TaskStateV1): TaskState = {
//    val opt1 = x.taskStateOneof.opt1
//    Seq(opt1).flatten.head match {
//      case y: UserTaskStateV1 => y
//    }
//  }
//}