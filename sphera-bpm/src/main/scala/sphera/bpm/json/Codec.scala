package sphera.bpm.json

import sphera.bpm.json.storage.StorageCodec
import sphera.bpm.json.storage.definition.PatchDefCodec
import io.circe.generic.AutoDerivation
//import io.circe.generic._
import io.circe.java8.time.TimeInstances
//import io.circe.syntax._
import io.circe.generic.auto._

trait Codec extends AutoDerivation
  // ExceptionMapping

  with TimeInstances
  // with ProductCodec
  with FiniteDurationCodec
  // with ModelCodec
  //with ExceptionCodec
  //with OperationCodec
  with StorageCodec {

  //  implicit val encodeCreateRunnableInfo: Encoder[CreateRunnableInfo] = {
  //    case x: CreateProjectInfo => x.asJson
  //    case x: CreateProcessInfo => x.asJson
  //    case x: CreateTaskInfo => x.asJson
  //  }

  //  val m =  ModifyAttr(Modifier(UUID.randomUUID()))
  //
  //
  //  val m1 = m.asJson
  //
  //
  //  val x = TaskInfo(
  //    id = ActorId("gfhfg"),
  //    name = "",
  //    description = None,
  //    taskType = TaskType.SubProcessTask,
  //    status = RunnableStatus.Created,
  //    initiator = Initiator.ProcessManager,
  //    ownerId = UUID.randomUUID(),
  //    assigneeId = None,
  //    reviewerId = None,
  //    watcherIds = Set.empty,
  //    parentId = None,
  //    childIds = Set.empty,
  //    activeChildIds = Set.empty,
  //    processId = None,
  //    subProcessId = None,
  //    operationId = None,
  //    operationBpmnId = None,
  //    formTemplateId = None,
  //    startedBy = None,
  //    startedOn = None,
  //    actualEnd = None,
  //    plannedStart = None,
  //    plannedDuration = None,
  //    modifyAttr = m).asJson
  //
  //
  //  import io.circe.parser._
  //
  //  val xx = x.as[TaskInfo]
  //

  //val x1 = x.asJson.as[TaskInfo]

}
