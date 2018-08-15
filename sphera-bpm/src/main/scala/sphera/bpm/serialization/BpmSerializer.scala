//package sphera.bpm.serialization
//
//import akka.serialization.SerializerWithStringManifest
//import sphera.bpm.definition.Definitions
//import sphera.bpm.history.actor.HistoryManagerActor.CreatedHistoryDataEvt
//import sphera.bpm.history.actor.HistoryManagerState
//import sphera.bpm.notification.actor._
//import sphera.bpm.process.actor.ProcessActor._
//import sphera.bpm.process.actor.ProcessManagerActor._
//import sphera.bpm.process.actor._
//import sphera.bpm.process.actor.operation._
//import sphera.bpm.project.actor.ProjectManagerActor._
//import sphera.bpm.project.actor.{ ProjectManagerState, ProjectState }
//import sphera.bpm.storage.`def`.DefStorage._
//import sphera.bpm.storage.ExceptionStorage._
//import sphera.bpm.task.actor.TaskManagerActor._
//import sphera.bpm.task.actor.{ TaskManagerState, UserTaskState }
//
//class BpmSerializer extends SerializerWithStringManifest
//  with ModelConverters
//  with StorageConverters
//  with ProcessConverters
//  with OperationConverters
//  with TaskConverters
//  with HistoryConverters
//  with NotificationConverters {
//
//  private val className = Option(this.getClass.getName).getOrElse("<undefined>")
//
//  override def identifier: Int = 20170921
//
//  override def toBinary(o: AnyRef): Array[Byte] = {
//    o match {
//      case obj: Definitions => toDefinitionsBinary(obj)
//
//      case obj: UpdatedDataElementEvt => toUpdatedDataElementEvtBinary(obj)
//      case obj: UpdatedDataEvt => toUpdatedDataEvtBinary(obj)
//      case obj: ExceptionEvt => toExceptionEvtBinary(obj)
//
//      case obj: ProjectManagerState => toProjectManagerStateBinary(obj)
//      case obj: CreatedProjectInfoEvt => toCreatedProjectInfoEvtBinary(obj)
//      case obj: UpdatedProjectInfoEvt => toUpdatedProjectInfoEvtBinary(obj)
//      case obj: DeletedProjectInfoEvt => toDeletedProjectInfoEvtBinary(obj)
//      case obj: ProjectState => toProjectStateBinary(obj)
//
//      case obj: ProcessManagerState => toProcessManagerStateBinary(obj)
//      case obj: CreatedProcessInfoEvt => toCreatedProcessInfoEvtBinary(obj)
//      case obj: UpdatedProcessInfoEvt => toUpdatedProcessInfoEvtBinary(obj)
//      case obj: DeletedProcessInfoEvt => toDeletedProcessInfoEvtBinary(obj)
//      case obj: ProcessState => toProcessStateBinary(obj)
//      case obj: ActivatedEvt => toActivatedEvtBinary(obj)
//
//      case obj: ParallelGatewayOperationActor.State => toParallelGatewayOperationStateBinary(obj)
//      case obj: UserTaskOperationActor.UserTaskOperationState => toUserTaskOperationStateBinary(obj)
//      case obj: OperationActor.ActivatedEvt => toOperationActivatedEvtBinary(obj)
//
//      case obj: TaskManagerState => toTaskManagerStateBinary(obj)
//      case obj: CreatedTaskInfoEvt => toCreatedTaskInfoEvtBinary(obj)
//      case obj: UpdatedTaskInfoEvt => toUpdatedTaskInfoEvtBinary(obj)
//      case obj: DeletedTaskInfoEvt => toDeletedTaskInfoEvtBinary(obj)
//      case obj: UserTaskState => toUserTaskStateBinary(obj)
//
//      case obj: HistoryManagerState => toHistoryStateBinary(obj)
//      case obj: CreatedHistoryDataEvt => toHCreatedHistoryDataEvtBinary(obj)
//
//      case obj: SmsVerificationState => toSmsVerificationStateBinary(obj)
//      case obj: SmsVerificationActor.CreatedVerificationEvt => toSmsVerificationCreatedVerificationEvtBinary(obj)
//      case obj: SmsVerificationActor.DeletedVerificationEvt => toSmsVerificationDeletedVerificationEvtBinary(obj)
//
//      case _ =>
//        val errorMsg = s"Can'template serialize an object using $className [${o.toString}]"
//        throw new IllegalArgumentException(errorMsg)
//    }
//  }
//
//  override def manifest(o: AnyRef): String = {
//    o match {
//      case _: Definitions => DefinitionsManifestV1
//
//      case _: UpdatedDataElementEvt => UpdatedDataElementEvtManifestV1
//      case _: UpdatedDataEvt => UpdatedDataEvtManifestV1
//      case _: ExceptionEvt => ExceptionEvtManifestV1
//
//      case _: ProjectManagerState => ProjectManagerStateManifestV1
//      case _: CreatedProjectInfoEvt => CreatedProjectInfoEvtManifestV1
//      case _: UpdatedProjectInfoEvt => UpdatedProjectInfoEvtManifestV1
//      case _: DeletedProjectInfoEvt => DeletedProjectInfoEvtManifestV1
//      case _: ProjectState => ProjectStateManifestV1
//
//      case _: ProcessManagerState => ProcessManagerStateManifestV1
//      case _: CreatedProcessInfoEvt => CreatedProcessInfoEvtManifestV1
//      case _: UpdatedProcessInfoEvt => UpdatedProcessInfoEvtManifestV1
//      case _: DeletedProcessInfoEvt => DeletedProcessInfoEvtManifestV1
//      case _: ProcessState => ProcessStateManifestV1
//      case _: ActivatedEvt => ActivatedEvtManifestV1
//
//      case _: ParallelGatewayOperationActor.State => ParallelGatewayOperationStateManifestV1
//      case _: UserTaskOperationActor.UserTaskOperationState => UserTaskOperationStateManifestV1
//      case _: OperationActor.ActivatedEvt => OperationActivatedEvtManifestV1
//
//      case _: TaskManagerState => TaskManagerStateManifestV1
//      case _: CreatedTaskInfoEvt => CreatedTaskInfoEvtManifestV1
//      case _: UpdatedTaskInfoEvt => UpdatedTaskInfoEvtManifestV1
//      case _: DeletedTaskInfoEvt => DeletedTaskInfoEvtManifestV1
//      case _: UserTaskState => UserTaskStateManifestV1
//
//      case _: HistoryManagerState => HistoryStateManifestV1
//      case _: CreatedHistoryDataEvt => CreatedHistoryDataEvtManifestV1
//
//      case _: SmsVerificationState => SmsVerificationStateManifestV1
//      case _: SmsVerificationActor.CreatedVerificationEvt => SmsVerificationCreatedVerificationEvtManifestV1
//      case _: SmsVerificationActor.DeletedVerificationEvt => SmsVerificationDeletedVerificationEvtManifestV1
//
//      case _ =>
//        val errorMsg = s"Can'template create manifest for object using $className [${o.toString}]"
//        throw new IllegalArgumentException(errorMsg)
//    }
//  }
//
//  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
//    manifest match {
//      case DefinitionsManifestV1 => fromDefinitionsBinary(bytes)
//
//      case UpdatedDataElementEvtManifestV1 => fromUpdatedDataElementEvtBinary(bytes)
//      case UpdatedDataEvtManifestV1 => fromUpdatedDataElementEvtBinary(bytes)
//      case ExceptionEvtManifestV1 => fromExceptionEvtBinary(bytes)
//
//      case ProjectManagerStateManifestV1 => fromProjectManagerStateBinary(bytes)
//      case CreatedProjectInfoEvtManifestV1 => fromCreatedProjectInfoEvtBinary(bytes)
//      case UpdatedProjectInfoEvtManifestV1 => fromUpdatedProjectInfoEvtBinary(bytes)
//      case DeletedProjectInfoEvtManifestV1 => fromDeletedProjectInfoEvtBinary(bytes)
//      case ProjectStateManifestV1 => fromProjectStateBinary(bytes)
//
//      case ProcessManagerStateManifestV1 => fromProcessManagerStateBinary(bytes)
//      case CreatedProcessInfoEvtManifestV1 => fromCreatedProcessInfoEvtBinary(bytes)
//      case UpdatedProcessInfoEvtManifestV1 => fromUpdatedProcessInfoEvtBinary(bytes)
//      case DeletedProcessInfoEvtManifestV1 => fromDeletedProcessInfoEvtBinary(bytes)
//      case ProcessStateManifestV1 => fromProcessStateBinary(bytes)
//      case ActivatedEvtManifestV1 => fromActivatedEvtBinary(bytes)
//
//      case ParallelGatewayOperationStateManifestV1 => fromParallelGatewayOperationStateBinary(bytes)
//      case UserTaskOperationStateManifestV1 => fromUserTaskOperationStateBinary(bytes)
//      case OperationActivatedEvtManifestV1 => fromOperationActivatedEvtBinary(bytes)
//
//      case TaskManagerStateManifestV1 => fromTaskManagerStateBinary(bytes)
//      case CreatedTaskInfoEvtManifestV1 => fromCreatedTaskInfoEvtBinary(bytes)
//      case UpdatedTaskInfoEvtManifestV1 => fromUpdatedTaskInfoEvtBinary(bytes)
//      case DeletedTaskInfoEvtManifestV1 => fromDeletedTaskInfoEvtBinary(bytes)
//      case UserTaskStateManifestV1 => fromUserTaskStateBinary(bytes)
//
//      case HistoryStateManifestV1 => fromHistoryStateBinary(bytes)
//      case CreatedHistoryDataEvtManifestV1 => fromCreatedHistoryDataEvtBinary(bytes)
//
//      case SmsVerificationStateManifestV1 => fromSmsVerificationState(bytes)
//      case SmsVerificationCreatedVerificationEvtManifestV1 => fromSmsVerificationCreatedVerificationEvt(bytes)
//      case SmsVerificationDeletedVerificationEvtManifestV1 => fromSmsVerificationDeletedVerificationEvt(bytes)
//
//      case _ =>
//        val errorMsg = s"Can'template deserialize an object using $className manifest [$manifest]"
//        throw new IllegalArgumentException(errorMsg)
//    }
//  }
//}