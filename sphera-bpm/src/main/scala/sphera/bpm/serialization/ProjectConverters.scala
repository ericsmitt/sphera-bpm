//package sphera.bpm.serialization
//
//import sphera.bpm.project._
//import sphera.bpm.project.actor.ProjectManagerActor._
//import sphera.bpm.project.actor.{ ProjectManagerState, ProjectState }
//import sphera.bpm.project.model._
//import sphera.bpm.serializer.proto.project._
//
//trait ProjectConverters extends DefinitionConverters
//  with ModelConverters
//  with LangConverters
//  with ExceptionConverters {
//  import Implicits._
//
//  val ProjectManagerStateManifestV1 = "ProjectManagerState.v1"
//  val CreatedProjectInfoEvtManifestV1 = "CreatedProjectInfoEvt.v1"
//  val UpdatedProjectInfoEvtManifestV1 = "UpdatedProjectInfoEvt.v1"
//  val DeletedProjectInfoEvtManifestV1 = "DeletedProjectInfoEvt.v1"
//
//  val ProjectStateManifestV1 = "ProjectState.v1"
//
//  def toProjectManagerStateBinary(obj: ProjectManagerState): Array[Byte] = {
//    val x = obj.v map { case (a, b) => a -> toProjectInfo(b) }
//    ProjectManagerStateV1(x).toByteArray
//  }
//
//  def fromProjectRuntimeState(x: ProjectManagerStateV1): ProjectManagerState = {
//    val y = x.v map { case (a, b) => a -> fromProjectInfo(b) }
//    ProjectManagerState(y)
//  }
//
//  def fromProjectManagerStateBinary(bytes: Array[Byte]): ProjectManagerState = {
//    val x = ProjectManagerStateV1.parseFrom(bytes).v.map { case (a, b) => (a, fromProjectInfo(b)) }
//    ProjectManagerState(x)
//  }
//
//  def toCreatedProjectInfoEvtBinary(obj: CreatedProjectInfoEvt): Array[Byte] = {
//    CreatedProjectInfoEvtV1(toProjectInfo(obj.x)).toByteArray
//  }
//
//  def toUpdatedProjectInfoEvtBinary(obj: UpdatedProjectInfoEvt): Array[Byte] = {
//    UpdatedProjectInfoEvtV1(toUpdateProjectInfo(obj.x), obj.modifier).toByteArray
//  }
//
//  def toDeletedProjectInfoEvtBinary(obj: DeletedProjectInfoEvt): Array[Byte] = {
//    DeletedProjectInfoEvtV1(projectId = obj.projectId).toByteArray
//  }
//
//  def fromUpdatedProjectInfoEvtBinary(bytes: Array[Byte]): UpdatedProjectInfoEvt = {
//    val x = UpdatedProjectInfoEvtV1.parseFrom(bytes)
//    UpdatedProjectInfoEvt(fromUpdateProjectInfo(x.x), x.modifier)
//  }
//
//  def fromCreatedProjectInfoEvtBinary(bytes: Array[Byte]): CreatedProjectInfoEvt = {
//    val x = CreatedProjectInfoEvtV1.parseFrom(bytes).x
//    CreatedProjectInfoEvt(fromProjectInfo(x))
//  }
//
//  def toUpdateProjectInfo(x: UpdateProjectInfo): UpdateProjectInfoV1 = {
//    UpdateProjectInfoV1(
//      id = x.id,
//      name = x.name,
//      description = x.description.flatten,
//      status = x.status.map(toProjectStatus),
//      managerId = x.managerId,
//      startedBy = x.startedBy.map(x => x.map(toModifier)).getOrElse(None),
//      startedOn = x.startedOn.map(zonedDateTimeOptToStringOpt).getOrElse(None))
//  }
//
//  def fromUpdateProjectInfo(x: UpdateProjectInfoV1): UpdateProjectInfo = {
//    UpdateProjectInfo(
//      id = x.id,
//      name = x.name,
//      description = x.description,
//      status = x.status.map(fromProjectStatus),
//      managerId = x.managerId,
//      startedBy = Option(x.startedBy.map(fromModifier)),
//      startedOn = Option(x.startedOn))
//  }
//
//  def fromDeletedProjectInfoEvtBinary(bytes: Array[Byte]): DeletedProjectInfoEvt = {
//    val x = DeletedProjectInfoEvtV1.parseFrom(bytes).projectId
//    DeletedProjectInfoEvt(projectId = x)
//  }
//
//  def toProjectStateBinary(obj: ProjectState): Array[Byte] = {
//    toProjectState(obj).toByteArray
//  }
//
//  implicit def toProjectState(x: ProjectState): ProjectStateV1 = {
//    ProjectStateV1(
//      projectDefinition = x.projectDef,
//      roleDefinitions = x.roleDefinitions,
//      definitions = x.defStorage,
//      exceptions = x.exceptions.map(toBpmException))
//  }
//
//  def fromProjectStateBinary(bytes: Array[Byte]): ProjectState = {
//    ProjectStateV1.parseFrom(bytes)
//  }
//
//  implicit def fromProjectState(x: ProjectStateV1): ProjectState = {
//    ProjectState(
//      projectDef = x.projectDefinition,
//      roleDefinitions = x.roleDefinitions,
//      defStorage = x.definitions,
//      exceptions = x.exceptions.map(fromBpmException))
//  }
//
//  implicit def toProjectDefinition(x: ProjectDef): ProjectDefinitionV1 = {
//    ProjectDefinitionV1(
//      roleDefinitions = x.roleDefinitions,
//      definitions = x.defStorage)
//  }
//
//  implicit def fromProjectDefinition(x: ProjectDefinitionV1): ProjectDef = {
//    ProjectDef(
//      roleDefinitions = x.roleDefinitions,
//      defStorage = x.definitions)
//  }
//
//  implicit def toProjectInfo(x: ProjectInfo): ProjectInfoV1 = {
//    ProjectInfoV1(
//      id = x.id,
//      name = x.name,
//      description = x.description,
//      status = x.status,
//      templateId = x.templateId,
//      managerId = x.managerId,
//      startedBy = x.startedBy.map(toModifier),
//      startedOn = x.startedOn,
//      modifyAttr = toModifyAttr(x.modifyAttr))
//  }
//
//  implicit def fromProjectInfo(x: ProjectInfoV1): ProjectInfo = {
//    ProjectInfo(
//      id = x.id,
//      name = x.name,
//      description = x.description,
//      status = x.status,
//      templateId = x.templateId,
//      managerId = x.managerId,
//      startedBy = x.startedBy.map(fromModifier),
//      startedOn = x.startedOn,
//      modifyAttr = fromModifyAttr(x.modifyAttr))
//  }
//
//  implicit def toProjectStatus(x: ProjectStatus): Int = {
//    x match {
//      case ProjectStatus.Created => 1
//      case ProjectStatus.Started => 2
//      case ProjectStatus.Completed => 3
//      case ProjectStatus.Deleted => 4
//      case ProjectStatus.Failed => 5
//      case _ => 1
//    }
//  }
//
//  implicit def fromProjectStatus(x: Int): ProjectStatus = {
//    x match {
//      case 1 => ProjectStatus.Created
//      case 2 => ProjectStatus.Started
//      case 3 => ProjectStatus.Completed
//      case 4 => ProjectStatus.Deleted
//      case 5 => ProjectStatus.Failed
//      case _ => ProjectStatus.Created
//    }
//  }
//
//  implicit def toProjectExtendedInfo(x: ProjectExtendedInfo): ProjectExtendedInfoV1 = {
//    ProjectExtendedInfoV1(
//      info = x.info,
//      state = x.state)
//  }
//
//  implicit def fromProjectExtendedInfo(x: ProjectExtendedInfoV1): ProjectExtendedInfo = {
//    ProjectExtendedInfo(
//      info = x.info,
//      state = x.state)
//  }
//}