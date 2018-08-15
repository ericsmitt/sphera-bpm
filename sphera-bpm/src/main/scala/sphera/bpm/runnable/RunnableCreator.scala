package sphera.bpm.runnable

import akka.actor.{ ActorRef, Props }
import sphera.bpm.bpmn.BpmnParser
import sphera.bpm.masterdata.model.ProcessTemplate
import sphera.bpm.model.{ Modifier, ModifyAttr }
import sphera.bpm.process.actor.{ ProcessActor, ProcessState }
import sphera.bpm.process.{ CreateProcessInfo, ProcessDef, ProcessInfo }
import sphera.bpm.project.actor.{ ProjectActor, ProjectState }
import sphera.bpm.project.{ CreateProjectInfo, ProjectDef, ProjectInfo }
import sphera.bpm.runnable.RunnableManager.{ CreateRunnableFailure, CreateRunnableResponse, CreateRunnableSuccess, Done }
import sphera.bpm.storage.definition.{ DefStorage, DefStorageBuilder }
import sphera.bpm.storage.runnable.RunnableStorage
import sphera.bpm.task._
import sphera.bpm.task.actor._
import sphera.bpm.{ BpmException, UnknownException }
import sphera.core.akkaext.actor.CqrsCommand
import sphera.core.utils.Generator

import scala.concurrent.Future

trait RunnableCreator extends Generator with DefStorageBuilder with BpmnParser { _: RunnableManagerBehavior =>
  def createProject(createInfo: CreateProjectInfo, defStorage: DefStorage, modifier: Modifier): Future[CreateRunnableResponse] = {
    val projectId = generateChildId

    val info = ProjectInfo(
      id = projectId,
      name = createInfo.name,
      description = createInfo.description,
      status = RunnableStatus.Created,
      parentId = createInfo.parentId,
      childIds = Set.empty,
      activeChildIds = Set.empty,
      templateId = createInfo.templateId,
      managerId = createInfo.managerId,
      relaredOperationId = createInfo.relatedOperationId,
      relatedOperationBpmnId = createInfo.relatedOperationBpmnId,
      startedBy = None,
      startedOn = None,
      modifyAttr = ModifyAttr(createdBy = modifier))

    val state = ProjectState(ProjectDef(defStorage))

    val props = ProjectActor.props(
      projectId = projectId,
      bpm = bpm,
      state = state)

    self.ask(RunnableCreator.CreateRunnableCmd(info, props, modifier)).map {
      case Done => CreateRunnableSuccess(info)
    }
  }

  def createProcess(
    template: ProcessTemplate,
    createInfo: CreateProcessInfo,
    defStorage: DefStorage,
    modifier: Modifier): Future[CreateRunnableResponse] = {
    val processId = generateChildId

    val info = ProcessInfo(
      id = processId,
      name = createInfo.name,
      description = createInfo.description,
      status = RunnableStatus.Created,
      bpmnId = template.bpmnId,
      parentId = createInfo.parentId,
      childIds = Set.empty,
      activeChildIds = Set.empty,
      projectId = createInfo.projectId,
      initiatorId = createInfo.initiatorId,
      templateId = createInfo.templateId,
      managerId = createInfo.managerId,
      operationIds = Set.empty,
      relaredOperationId = createInfo.relatedOperationId,
      relatedOperationBpmnId = createInfo.relatedOperationBpmnId,
      startedBy = None,
      startedOn = None,
      modifyAttr = ModifyAttr(createdBy = modifier))

    val state = createInfo
      .initState
      .getOrElse(ProcessState(ProcessDef(defStorage, template.opDefs)))

    val props = ProcessActor.props(
      processId = processId,
      projectId = createInfo.projectId,
      bpm = bpm,
      state = state)

    self.ask(RunnableCreator.CreateRunnableCmd(info, props, modifier)).map {
      case Done => CreateRunnableSuccess(info)
    }
  }

  def createTask(createInfo: CreateTaskInfo, modifier: Modifier): Future[CreateRunnableResponse] = {
    val taskId = generateChildId

    val info = TaskInfo(
      id = taskId,
      name = createInfo.name,
      description = createInfo.description,
      taskType = createInfo.taskType,
      status = RunnableStatus.Created,
      initiator = createInfo.initiator,
      ownerId = createInfo.ownerId,
      assigneeId = createInfo.assigneeId,
      reviewerId = createInfo.reviewerId,
      watcherIds = createInfo.watcherIds,
      parentId = createInfo.parentId,
      childIds = Set.empty,
      activeChildIds = Set.empty,
      processId = createInfo.processId,
      subProcessId = createInfo.subProcessId,
      relaredOperationId = createInfo.relatedOperationId,
      relatedOperationBpmnId = createInfo.relatedOperationBpmnId,
      formTemplateId = createInfo.formTemplateId,
      startedBy = None,
      startedOn = None,
      actualEnd = None,
      plannedStart = createInfo.plannedStart,
      plannedDuration = createInfo.plannedDuration,
      modifyAttr = ModifyAttr(createdBy = modifier))

    val props = info.taskType match {
      case TaskType.UserTask =>
        UserTaskActor.props(
          taskId = taskId,
          bpm = bpm,
          state = UserTaskState(RunnableStorage(createInfo.defStorage)))
      case TaskType.SendTask =>
        SendTaskActor.props(
          taskId = taskId,
          bpm = bpm,
          state = SendTaskState(RunnableStorage(createInfo.defStorage)))
      case TaskType.ScriptTask =>
        ScriptTaskActor.props(
          taskId = taskId,
          bpm = bpm,
          state = ScriptTaskState(RunnableStorage(createInfo.defStorage)))
      case TaskType.SubProcessTask =>
        SubProcessTaskActor.props(
          taskId = taskId,
          processId = createInfo.processId.get,
          subProcessId = createInfo.subProcessId.get,
          bpm = bpm,
          state = SubProcessTaskState(RunnableStorage(createInfo.defStorage)))
      case _ => sys.error("Invalid task type")
    }

    self.ask(RunnableCreator.CreateRunnableCmd(info, props, modifier)).map {
      case Done => CreateRunnableSuccess(info)
    }
  }

  def preCreateRunnable(state: State, createInfo: CreateRunnableInfo, recoverInfo: Option[RecoverInfo], from: ActorRef, modifier: Modifier): Unit = {
    def preCreate(createInfo: CreateRunnableInfo): Unit = {
      (createInfo match {
        case x: CreateProjectInfo =>
          for {
            r1 <- bpm.masterDataManager.getProjectTemplate(x.templateId)
            r2 <- build(dataStructureId = r1.dataStructureId, data = x.data)
            r3 <- createProject(createInfo = x, defStorage = r2, modifier = modifier)
          } yield r3
        case x: CreateProcessInfo =>
          for {
            r1 <- bpm.masterDataManager.getProcessTemplate(x.templateId)
            r2 <- build(dataStructureId = r1.dataStructureId, data = x.data)
            r3 <- createProcess(template = r1, createInfo = x, defStorage = r2, modifier = modifier)
          } yield r3
        case x: CreateTaskInfo =>
          x.taskType match {
            case TaskType.UserTask => createTask(createInfo = x, modifier = modifier)
            case TaskType.SendTask => createTask(createInfo = x, modifier = modifier)
            case TaskType.ScriptTask => createTask(createInfo = x, modifier = modifier)
            case TaskType.SubProcessTask => createTask(createInfo = x, modifier = modifier)
            case _ => sys.error("invalid task type")
          }
      }) recover creatorExceptionHandler pipeTo from
    }

    /**
     * Recover create
     */
    recoverInfo match {
      case Some(_) =>
        val runnableType = createInfo.runnableType
        log.warning(s"trying recover create $runnableType")
        createInfo.relatedOperationId match {
          case Some(operationId) =>
            state.getRunnableInfoByOperation(operationId) match {
              case Some(info) =>
                log.warning(s"found $runnableType info related to operation [${runnableType}Id: ${info.id}]")
                after(getRunnableOpt(state, info.id)) { (response, state) =>
                  response match {
                    case Some(task) =>
                      log.warning(s"found $runnableType actor [${runnableType}Id: ${info.id}]")
                      from ! CreateRunnableSuccess(info)
                    case None =>
                      log.warning(s"trying recover $runnableType actor [${runnableType}Id: ${info.id}]")
                      preCreate(createInfo)
                  }
                }
              case None =>
                log.warning(s"trying recreate $runnableType")
                preCreate(createInfo)
            }
        }
      case None => preCreate(createInfo)
    }
  }

  val creatorExceptionHandler: PartialFunction[Throwable, CreateRunnableFailure] = {
    case e: BpmException =>
      e.printStackTrace()
      CreateRunnableFailure(List(e))
    case e: Throwable =>
      e.printStackTrace()
      CreateRunnableFailure(List(UnknownException(e.getMessage)))
  }
}

object RunnableCreator {
  trait Command extends CqrsCommand

  case class CreateRunnableCmd(info: RunnableInfo, props: Props, modifier: Modifier) extends Command
}