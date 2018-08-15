package sphera.bpm.masterdata

import java.util.UUID

import akka.actor.ActorRef
import sphera.bpm.{ EntityAlreadyExistsException, EntityNotFoundException }
import sphera.bpm.masterdata.actor.ProcessTemplatesRepositoryActor.ProcessTemplateOpt
import sphera.bpm.masterdata.actor._
import sphera.bpm.masterdata.model.CalendarType.CalendarTypes
import sphera.bpm.masterdata.model.Characteristic.Characteristics
import sphera.bpm.masterdata.model.FileType.FileTypes
import sphera.bpm.masterdata.model.FormTemplate.FormTemplates
import sphera.bpm.masterdata.model.ProcessTemplate.ProcessTemplates
import sphera.bpm.masterdata.model.ProjectTemplate.ProjectTemplates
import sphera.bpm.masterdata.model.RoleType.RoleTypes
import sphera.bpm.masterdata.model._
import sphera.bpm.model._
import sphera.core.akkaext.actor.{ CqrsResponse, FutureSupport }

import scala.concurrent.Future

trait MasterDataManagerLike extends FutureSupport {
  protected def masterDataManagerActor: ActorRef

  private def valueOrNotFound[T <: Any](entityId: UUID, entityType: EntityType): PartialFunction[Option[T], T] = {
    case Some(x) => x
    case None => throw EntityNotFoundException(entityId, entityType)
  }

  private def ifDone(entityId: UUID): PartialFunction[CqrsResponse, UUID] = {
    case DataStructuresRepositoryActor.Done => entityId
    case FormTemplatesRepositoryActor.Done => entityId
    case ProcessTemplatesRepositoryActor.Done => entityId
    case ProjectTemplatesRepositoryActor.Done => entityId
    case FormTemplatesRepositoryActor.Done => entityId
    case RoleTypesRepositoryActor.Done => entityId
    case FileTypesRepositoryActor.Done => entityId
    case CharacteristicsRepositoryActor.Done => entityId
    case CalendarTypesRepositoryActor.Done => entityId
  }

  private def idOrNotFound(entityId: UUID, entityType: EntityType): PartialFunction[CqrsResponse, UUID] = {
    ifDone(entityId).orElse {
      case _ => throw EntityNotFoundException(entityId, entityType)
    }
  }

  private def idOrAlreadyExists(entityId: UUID, entityType: EntityType): PartialFunction[CqrsResponse, UUID] = {
    ifDone(entityId).orElse {
      case _ => throw EntityAlreadyExistsException(entityId, entityType)
    }
  }

  def getDataStructure(dataStructureId: DataStructure.Id): Future[DataStructure] =
    ask(masterDataManagerActor, DataStructuresRepositoryActor.GetById(dataStructureId))
      .mapTo[DataStructuresRepositoryActor.DataStructureOpt]
      .map(_.x)
      .map(valueOrNotFound(dataStructureId, EntityType.DataStructure))

  def getDataStructureMap: Future[Map[DataStructure.Id, DataStructure]] =
    ask(masterDataManagerActor, DataStructuresRepositoryActor.GetAll)
      .mapTo[DataStructuresRepositoryActor.DataStructureMap]
      .map(_.x)

  def getDataStructureSet: Future[Set[DataStructure]] =
    getDataStructureMap
      .map(_.values.toSet)

  def createDataStructure(x: UpdateDataStructure): Future[DataStructure.Id] =
    ask(masterDataManagerActor, DataStructuresRepositoryActor.CreateCmd(x))
      .mapTo[DataStructuresRepositoryActor.Response]
      .map(idOrAlreadyExists(x.id, EntityType.DataStructure))

  def createAndGetDataStructure(x: UpdateDataStructure): Future[DataStructure] =
    for {
      _ <- ask(masterDataManagerActor, DataStructuresRepositoryActor.CreateCmd(x))
      x <- ask(masterDataManagerActor, DataStructuresRepositoryActor.GetById(x.id))
        .mapTo[DataStructuresRepositoryActor.DataStructureOpt]
        .map(_.x.get)
    } yield x

  def updateDataStructure(x: UpdateDataStructure): Future[DataStructure.Id] =
    ask(masterDataManagerActor, DataStructuresRepositoryActor.UpdateCmd(x))
      .mapTo[DataStructuresRepositoryActor.Response]
      .map(idOrNotFound(x.id, EntityType.DataStructure))

  def deleteDataStructure(dataStructureId: DataStructure.Id): Future[DataStructure.Id] =
    ask(masterDataManagerActor, DataStructuresRepositoryActor.DeleteCmd(dataStructureId))
      .mapTo[DataStructuresRepositoryActor.Response]
      .map(idOrNotFound(dataStructureId, EntityType.DataStructure))

  def getFormDataStructureId(formTemplateId: FormTemplate.Id): Future[DataStructure.Id] =
    ask(masterDataManagerActor, FormTemplatesRepositoryActor.GetById(formTemplateId))
      .mapTo[FormTemplatesRepositoryActor.FormTemplateOpt]
      .map(_.x.get.dataStructureId)

  def getFormTemplate(formTemplateId: FormTemplate.Id): Future[FormTemplate] =
    ask(masterDataManagerActor, FormTemplatesRepositoryActor.GetById(formTemplateId))
      .mapTo[FormTemplatesRepositoryActor.FormTemplateOpt]
      .map(_.x)
      .map(valueOrNotFound(formTemplateId, EntityType.FormTemplate))

  def getFormTemplates: Future[FormTemplates] =
    ask(masterDataManagerActor, FormTemplatesRepositoryActor.GetAll)
      .mapTo[FormTemplatesRepositoryActor.FormTemplateMap]
      .map(_.x)

  def getFormTemplatesAsSet: Future[Set[FormTemplate]] =
    getFormTemplates
      .map(_.values.toSet)

  def createFormTemplate(x: UpdateFormTemplate): Future[FormTemplate.Id] =
    ask(masterDataManagerActor, FormTemplatesRepositoryActor.CreateCmd(x))
      .mapTo[FormTemplatesRepositoryActor.Response]
      .map(idOrAlreadyExists(x.id, EntityType.FormTemplate))

  def createAndGetFormTemplate(x: UpdateFormTemplate): Future[FormTemplate] =
    for {
      _ <- ask(masterDataManagerActor, FormTemplatesRepositoryActor.CreateCmd(x))
      x <- ask(masterDataManagerActor, FormTemplatesRepositoryActor.GetById(x.id))
        .mapTo[FormTemplatesRepositoryActor.FormTemplateOpt]
        .map(_.x.get)
    } yield x

  def updateFormTemplate(x: UpdateFormTemplate): Future[FormTemplate.Id] =
    ask(masterDataManagerActor, FormTemplatesRepositoryActor.UpdateCmd(x))
      .mapTo[FormTemplatesRepositoryActor.Response]
      .map(idOrNotFound(x.id, EntityType.FormTemplate))

  def deleteFormTemplate(formTemplateId: FormTemplate.Id): Future[FormTemplate.Id] =
    ask(masterDataManagerActor, FormTemplatesRepositoryActor.DeleteCmd(formTemplateId))
      .mapTo[FormTemplatesRepositoryActor.Response]
      .map(idOrNotFound(formTemplateId, EntityType.FormTemplate))

  def getProcessTemplate(processTemplateId: ProcessTemplate.Id): Future[ProcessTemplate] =
    ask(masterDataManagerActor, ProcessTemplatesRepositoryActor.GetProcessTemplateById(processTemplateId))
      .mapTo[ProcessTemplatesRepositoryActor.ProcessTemplateOpt]
      .map(_.x)
      .map(valueOrNotFound(processTemplateId, EntityType.ProcessTemplate))

  def getProcessTemplates: Future[ProcessTemplates] =
    ask(masterDataManagerActor, ProcessTemplatesRepositoryActor.GetProcessTemplateAll)
      .mapTo[ProcessTemplatesRepositoryActor.ProcessTemplateMap]
      .map(_.x)

  def getProcessTemplatesAsSet: Future[Set[ProcessTemplate]] =
    getProcessTemplates
      .map(_.values.toSet)

  def createProcessTemplate(x: UpdateProcessTemplate): Future[ProcessTemplate.Id] =
    ask(masterDataManagerActor, ProcessTemplatesRepositoryActor.CreateProcessTemplateCmd(x))
      .mapTo[ProcessTemplatesRepositoryActor.Response]
      .map {
        case ProcessTemplatesRepositoryActor.AlreadyExists => throw EntityAlreadyExistsException(x.id, EntityType.ProcessTemplate)
        case ProcessTemplatesRepositoryActor.InvalidTemplate(exception) => throw exception
        case ProcessTemplatesRepositoryActor.InvalidOperationTemplateData(exception) => throw exception
        case ProcessTemplatesRepositoryActor.Done => x.id
      }

  def createAndGetProcessTemplate(x: UpdateProcessTemplate): Future[ProcessTemplate] =
    createProcessTemplate(x)
      .flatMap { x =>
        masterDataManagerActor.ask(ProcessTemplatesRepositoryActor.GetProcessTemplateById(x))
          .mapTo[ProcessTemplateOpt]
          .map(_.x.get)
      }

  def updateProcessTemplate(x: UpdateProcessTemplate): Future[ProcessTemplate.Id] =
    ask(masterDataManagerActor, ProcessTemplatesRepositoryActor.UpdateProcessTemplateCmd(x))
      .mapTo[ProcessTemplatesRepositoryActor.Response]
      .map(idOrNotFound(x.id, EntityType.ProcessTemplate))

  def deleteProcessTemplate(processTemplateId: ProcessTemplate.Id): Future[ProcessTemplate.Id] =
    ask(masterDataManagerActor, ProcessTemplatesRepositoryActor.DeleteProcessTemplateCmd(processTemplateId))
      .mapTo[ProcessTemplatesRepositoryActor.Response]
      .map(idOrNotFound(processTemplateId, EntityType.ProcessTemplate))

  def getProjectTemplate(projectTemplateId: ProjectTemplate.Id): Future[ProjectTemplate] =
    ask(masterDataManagerActor, ProjectTemplatesRepositoryActor.GetById(projectTemplateId))
      .mapTo[ProjectTemplatesRepositoryActor.ProjectTemplateOpt]
      .map(_.x)
      .map(valueOrNotFound(projectTemplateId, EntityType.ProjectTemplate))

  def getProjectTemplates: Future[ProjectTemplates] =
    ask(masterDataManagerActor, ProjectTemplatesRepositoryActor.GetAll)
      .mapTo[ProjectTemplatesRepositoryActor.ProjectTemplateMap]
      .map(_.x)

  def getProjectTemplatesAsSet: Future[Set[ProjectTemplate]] =
    getProjectTemplates
      .map(_.values.toSet)

  def createProjectTemplate(x: UpdateProjectTemplate): Future[ProjectTemplate.Id] =
    ask(masterDataManagerActor, ProjectTemplatesRepositoryActor.CreateCmd(x))
      .mapTo[ProjectTemplatesRepositoryActor.Response]
      .map(idOrAlreadyExists(x.id, EntityType.ProjectTemplate))

  def createAndGetProjectTemplate(x: UpdateProjectTemplate): Future[ProjectTemplate] =
    for {
      _ <- ask(masterDataManagerActor, ProjectTemplatesRepositoryActor.CreateCmd(x))
      x <- ask(masterDataManagerActor, ProjectTemplatesRepositoryActor.GetById(x.id))
        .mapTo[ProjectTemplatesRepositoryActor.ProjectTemplateOpt]
        .map(_.x.get)
    } yield x

  def updateProjectTemplate(x: UpdateProjectTemplate): Future[ProjectTemplate.Id] =
    ask(masterDataManagerActor, ProjectTemplatesRepositoryActor.UpdateCmd(x))
      .mapTo[ProjectTemplatesRepositoryActor.Response]
      .map(idOrNotFound(x.id, EntityType.ProjectTemplate))

  def deleteProjectTemplate(projectTemplateId: ProjectTemplate.Id): Future[ProjectTemplate.Id] =
    ask(masterDataManagerActor, ProjectTemplatesRepositoryActor.DeleteCmd(projectTemplateId))
      .mapTo[ProjectTemplatesRepositoryActor.Response]
      .map(idOrNotFound(projectTemplateId, EntityType.ProjectTemplate))

  def getRoleType(roleTypeId: RoleType.Id): Future[RoleType] =
    ask(masterDataManagerActor, RoleTypesRepositoryActor.GetById(roleTypeId))
      .mapTo[RoleTypesRepositoryActor.RoleTypeOpt]
      .map(_.x)
      .map(valueOrNotFound(roleTypeId, EntityType.RoleType))

  def getRoleTypes: Future[RoleTypes] =
    ask(masterDataManagerActor, RoleTypesRepositoryActor.GetAll)
      .mapTo[RoleTypesRepositoryActor.RoleTypeMap]
      .map(_.x)

  def getRoleTypesAsSet: Future[Set[RoleType]] =
    getRoleTypes
      .map(_.values.toSet)

  def createRoleType(x: UpdateRoleType): Future[RoleType.Id] =
    ask(masterDataManagerActor, RoleTypesRepositoryActor.CreateCmd(x))
      .mapTo[RoleTypesRepositoryActor.Response]
      .map(idOrAlreadyExists(x.id, EntityType.RoleType))

  def createAndGetRoleType(x: UpdateRoleType): Future[RoleType] =
    for {
      _ <- ask(masterDataManagerActor, RoleTypesRepositoryActor.CreateCmd(x))
      x <- ask(masterDataManagerActor, RoleTypesRepositoryActor.GetById(x.id))
        .mapTo[RoleTypesRepositoryActor.RoleTypeOpt]
        .map(_.x.get)
    } yield x

  def updateRoleType(x: UpdateRoleType): Future[RoleType.Id] =
    ask(masterDataManagerActor, RoleTypesRepositoryActor.UpdateCmd(x))
      .mapTo[RoleTypesRepositoryActor.Response]
      .map(idOrNotFound(x.id, EntityType.RoleType))

  def deleteRoleType(roleTypeId: RoleType.Id): Future[RoleType.Id] =
    ask(masterDataManagerActor, RoleTypesRepositoryActor.DeleteCmd(roleTypeId))
      .mapTo[RoleTypesRepositoryActor.Response]
      .map(idOrNotFound(roleTypeId, EntityType.RoleType))

  def getFileType(fileTypeId: FileType.Id): Future[FileType] =
    ask(masterDataManagerActor, FileTypesRepositoryActor.GetById(fileTypeId))
      .mapTo[FileTypesRepositoryActor.FileTypeOpt]
      .map(_.x)
      .map(valueOrNotFound(fileTypeId, EntityType.FileType))

  def getFileTypes: Future[FileTypes] =
    ask(masterDataManagerActor, FileTypesRepositoryActor.GetAll)
      .mapTo[FileTypesRepositoryActor.FileTypeMap]
      .map(_.x)

  def getFileTypesAsSet: Future[Set[FileType]] =
    getFileTypes
      .map(_.values.toSet)

  def createFileType(x: UpdateFileType): Future[FileType.Id] =
    ask(masterDataManagerActor, FileTypesRepositoryActor.CreateCmd(x))
      .mapTo[FileTypesRepositoryActor.Response]
      .map(idOrAlreadyExists(x.id, EntityType.FileType))

  def createAndGetFileType(x: UpdateFileType): Future[FileType] =
    for {
      _ <- ask(masterDataManagerActor, FileTypesRepositoryActor.CreateCmd(x))
      x <- ask(masterDataManagerActor, FileTypesRepositoryActor.GetById(x.id))
        .mapTo[FileTypesRepositoryActor.FileTypeOpt]
        .map(_.x.get)
    } yield x

  def updateFileType(x: UpdateFileType): Future[FileType.Id] =
    ask(masterDataManagerActor, FileTypesRepositoryActor.UpdateCmd(x))
      .mapTo[FileTypesRepositoryActor.Response]
      .map(idOrNotFound(x.id, EntityType.FileType))

  def deleteFileType(fileTypeId: FileType.Id): Future[FileType.Id] =
    ask(masterDataManagerActor, FileTypesRepositoryActor.DeleteCmd(fileTypeId))
      .mapTo[FileTypesRepositoryActor.Response]
      .map(idOrNotFound(fileTypeId, EntityType.FileType))

  def getCharacteristic(characteristicId: Characteristic.Id): Future[Characteristic] =
    ask(masterDataManagerActor, CharacteristicsRepositoryActor.GetById(characteristicId))
      .mapTo[CharacteristicsRepositoryActor.CharacteristicOpt]
      .map(_.x)
      .map(valueOrNotFound(characteristicId, EntityType.Characteristic))

  def getCharacteristics: Future[Characteristics] =
    ask(masterDataManagerActor, CharacteristicsRepositoryActor.GetAll)
      .mapTo[CharacteristicsRepositoryActor.CharacteristicMap]
      .map(_.x)

  def getCharacteristicsAsSet: Future[Set[Characteristic]] =
    getCharacteristics
      .map(_.values.toSet)

  def createCharacteristic(x: UpdateCharacteristic): Future[Characteristic.Id] =
    ask(masterDataManagerActor, CharacteristicsRepositoryActor.CreateCmd(x))
      .mapTo[CharacteristicsRepositoryActor.Response]
      .map(idOrAlreadyExists(x.id, EntityType.Characteristic))

  def createAndGetCharacteristic(x: UpdateCharacteristic): Future[Characteristic] =
    for {
      _ <- ask(masterDataManagerActor, CharacteristicsRepositoryActor.CreateCmd(x))
      x <- ask(masterDataManagerActor, CharacteristicsRepositoryActor.GetById(x.id))
        .mapTo[CharacteristicsRepositoryActor.CharacteristicOpt]
        .map(_.x.get)
    } yield x

  def updateCharacteristic(x: UpdateCharacteristic): Future[Characteristic.Id] =
    ask(masterDataManagerActor, CharacteristicsRepositoryActor.UpdateCmd(x))
      .mapTo[CharacteristicsRepositoryActor.Response]
      .map(idOrNotFound(x.id, EntityType.Characteristic))

  def deleteCharacteristic(characteristicId: Characteristic.Id): Future[Characteristic.Id] =
    ask(masterDataManagerActor, CharacteristicsRepositoryActor.DeleteCmd(characteristicId))
      .mapTo[CharacteristicsRepositoryActor.Response]
      .map(idOrNotFound(characteristicId, EntityType.Characteristic))

  def getCalendarType(calendarTypeId: CalendarType.Id): Future[CalendarType] =
    ask(masterDataManagerActor, CalendarTypesRepositoryActor.GetById(calendarTypeId))
      .mapTo[CalendarTypesRepositoryActor.CalendarTypeOpt]
      .map(_.x)
      .map(valueOrNotFound(calendarTypeId, EntityType.CalendarType))

  def getCalendarTypes: Future[CalendarTypes] =
    ask(masterDataManagerActor, CalendarTypesRepositoryActor.GetAll)
      .mapTo[CalendarTypesRepositoryActor.CalendarTypeMap]
      .map(_.x)

  def createCalendarType(x: UpdateCalendarType): Future[CalendarType.Id] =
    ask(masterDataManagerActor, CalendarTypesRepositoryActor.CreateCmd(x))
      .mapTo[CalendarTypesRepositoryActor.Response]
      .map(idOrAlreadyExists(x.id, EntityType.CalendarType))

  def createAndGetCalendarType(x: UpdateCalendarType): Future[CalendarType] =
    for {
      _ <- ask(masterDataManagerActor, CalendarTypesRepositoryActor.CreateCmd(x))
      x <- ask(masterDataManagerActor, CalendarTypesRepositoryActor.GetById(x.id))
        .mapTo[CalendarTypesRepositoryActor.CalendarTypeOpt]
        .map(_.x.get)
    } yield x

  def updateCalendarType(x: UpdateCalendarType): Future[CalendarType.Id] =
    ask(masterDataManagerActor, CalendarTypesRepositoryActor.UpdateCmd(x))
      .mapTo[CalendarTypesRepositoryActor.Response]
      .map(idOrNotFound(x.id, EntityType.CalendarType))

  def deleteCalendarType(calendarTypeId: CalendarType.Id): Future[CalendarType.Id] =
    ask(masterDataManagerActor, CalendarTypesRepositoryActor.DeleteCmd(calendarTypeId))
      .mapTo[CalendarTypesRepositoryActor.Response]
      .map(idOrNotFound(calendarTypeId, EntityType.CalendarType))
}
