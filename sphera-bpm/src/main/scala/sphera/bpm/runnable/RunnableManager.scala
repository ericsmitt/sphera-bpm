package sphera.bpm.runnable

import java.time.ZonedDateTime

import akka.actor.SupervisorStrategy.{ Escalate, Resume }
import akka.actor.{ ActorRef, OneForOneStrategy, Props }
import akka.pattern.AskSupport
import akka.util.Timeout
import sphera.bpm.history.HistoryManager
import sphera.bpm.history.model.HistoryData
import sphera.bpm.model.{ EntityType, Modifier }
import sphera.bpm.process.actor.{ ProcessActor, ProcessManagerState }
import sphera.bpm.process.{ Operation, Process, ProcessInfo, ProcessManager, UpdateProcessInfo }
import sphera.bpm.project.actor.{ ProjectActor, ProjectManagerState }
import sphera.bpm.project.{ Project, ProjectInfo, ProjectManager, UpdateProjectInfo }
import sphera.bpm.runnable.RunnableManager._
import sphera.bpm.runnable.RunnableManagerType._
import sphera.bpm.storage.actor.NodeContainerBehavior
import sphera.bpm.storage.runnable.RunnableStorageManagerLike
import sphera.bpm.task._
import sphera.bpm.task.actor._
import sphera.bpm.utils.Printable
import sphera.bpm.{ Bpm, BpmException, CreateRunnableException, EntityNotFoundException }
import sphera.core.akkaext.actor._
import sphera.core.akkaext.persistence.CqrsPersistentActorLike

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

trait RunnableManager extends RunnableStorageManagerLike
  with AskSupport
  with RunnableManagerTypes {

  def bpm: Bpm
  def managerType: RunnableManagerType
  def managerActor: ActorRef
  def historyManager: HistoryManager
  def nodeContainerActor: ActorRef = managerActor

  def valueOrNotFound[T <: Any](runnableId: Runnable.Id): PartialFunction[Option[T], T] = {
    case Some(x) => x
    case None =>
      val entityType = this match {
        case x: ProjectManager => EntityType.ProjectInfo
        case x: ProcessManager => EntityType.ProcessInfo
        case x: TaskManager => EntityType.TaskInfo
      }
      throw EntityNotFoundException(runnableId.raw, entityType)
  }

  def idOrNotFound(runnableId: Runnable.Id): PartialFunction[CqrsResponse, Runnable.Id] = {
    case RunnableManager.Done => runnableId
    case _ =>
      val entityType = managerType match {
        case ProjectManagerType => EntityType.ProjectInfo
        case ProcessManagerType => EntityType.ProcessInfo
        case TaskManagerType => EntityType.TaskInfo
      }
      throw EntityNotFoundException(runnableId.raw, entityType)
  }

  def getRunnablesIdSet: Future[Set[Runnable.Id]] =
    ask(managerActor, GetRunnableInfoMap)
      .mapTo[RunnableInfoMap]
      .map(_.x)
      .map(_.values.map(_.id).toSet)

  def getRunnableInfo(runnableId: Runnable.Id): Future[Info] =
    ask(managerActor, GetRunnableInfoById(runnableId))
      .mapTo[RunnableInfoOpt]
      .map(_.x)
      .map(valueOrNotFound(runnableId))
      .mapTo[Info]

  def getRunnableInfoOpt(runnableId: Runnable.Id): Future[Option[Info]] =
    ask(managerActor, GetRunnableInfoById(runnableId))
      .mapTo[RunnableInfoOpt]
      .map(_.x)
      .mapTo[Option[Info]]

  def getRunnableInfoMap: Future[Map[RawId, Info]] =
    ask(managerActor, GetRunnableInfoMap)
      .mapTo[RunnableInfoMap]
      .map(_.x)
      .mapTo[Map[RawId, Info]]

  def getRunnableInfoSet: Future[Set[Info]] =
    getRunnableInfoMap
      .map(_.values.toSet)

  def getRunnableExtendedInfo(runnableId: Runnable.Id): Future[ExtendedInfo] =
    ask(managerActor, GetRunnableExtendedInfo(runnableId))
      .mapTo[RunnableExtendedInfoOpt]
      .map(_.x)
      .map(valueOrNotFound(runnableId))
      .mapTo[ExtendedInfo]

  def getRunnableExtendedInfoMap: Future[Map[RawId, ExtendedInfo]] =
    ask(managerActor, GetRunnableExtendedInfoMap)
      .mapTo[RunnableExtendedInfoMap]
      .map(_.x)
      .mapTo[Map[RawId, ExtendedInfo]]

  def getRunnableState(runnableId: Runnable.Id): Future[ConcreteRunnableState] =
    getRunnable(runnableId)
      .flatMap(_.getState)
      .mapTo[ConcreteRunnableState]

  def getRunnableStatus(runnableId: Runnable.Id): Future[RunnableStatus] =
    ask(managerActor, GetRunnableStatus(runnableId))
      .mapTo[RunnableStatusOpt]
      .map(_.x)
      .map(valueOrNotFound(runnableId))

  def isRunnableStarted(runnableId: Runnable.Id): Future[Boolean] =
    ask(managerActor, GetRunnableStatus(runnableId))
      .mapTo[RunnableStatusOpt]
      .map(_.x.map(_ == RunnableStatus.Started))
      .map(valueOrNotFound(runnableId))

  def getRunnable(runnableId: Runnable.Id): Future[ConcreteRunnable] =
    getRunnableOpt(runnableId).map(_.get)

  def getRunnableOpt(runnableId: Runnable.Id): Future[Option[ConcreteRunnable]] =
    getNodeOpt(runnableId)
      .map(nodeOpt => nodeOpt.x match {
        case Some(actorRef) => managerType match {
          case ProjectManagerType => Some(new Project(actorRef))
          case ProcessManagerType => Some(new Process(actorRef))
          case TaskManagerType => ctag6.runtimeClass match {
            case x if x isAssignableFrom classOf[UserTask] => Some(new UserTask(actorRef))
            case x if x isAssignableFrom classOf[SendTask] => Some(new SendTask(actorRef))
            case x if x isAssignableFrom classOf[ScriptTask] => Some(new ScriptTask(actorRef))
            case x => sys.error("Invalid task type")
          }
        }
        case None => None
      })
      .mapTo[Option[ConcreteRunnable]]

  def getRunnableActorRef(runnableId: Runnable.Id): Future[ActorRef] =
    getNode(runnableId)

  def getRunnableActorRefOpt(runnableId: Runnable.Id): Future[Option[ActorRef]] =
    getNodeOpt(runnableId).map(_.x)

  def getRunnableActorRefSet: Future[Set[ActorRef]] =
    getRunnableInfoMap.flatMap { x =>
      Future.sequence(x.values.map(x => getRunnableActorRef(x.id)))
        .map(_.toSet)
    }

  def forward(runnableId: Runnable.Id, childId: ActorId, message: Any): Unit =
    managerActor ! Forward(runnableId, childId, message)

  def forward(runnableId: Runnable.Id, message: Any) =
    getRunnableActorRef(runnableId).map(_.ask(message))

  def createRunnable(createInfo: CreateRunnableInfo, recoverInfo: Option[RecoverInfo])(implicit modifier: Modifier): Future[Info] =
    ask(managerActor, CreateRunnableCmd(createInfo, recoverInfo, modifier))
      .mapTo[CreateRunnableResponse]
      .map {
        case CreateRunnableSuccess(x: Info) => x
        case CreateRunnableFailure(x) => throw CreateRunnableException(x)
      }

  def createRunnableN(createInfo: CreateRunnableInfo, n: Int, recoverInfo: Option[RecoverInfo])(implicit modifier: Modifier): Future[List[Info]] =
    Future.sequence(for (_ <- 1 to n) yield createRunnable(createInfo, recoverInfo))
      .map(_.toList)

  def createRunnableAndGetId(create: CreateRunnableInfo, recoverInfo: Option[RecoverInfo])(implicit modifier: Modifier): Future[Runnable.Id] =
    createRunnable(create, recoverInfo).map(_.id)

  def createRunnableAndGet(create: CreateRunnableInfo, recoverInfo: Option[RecoverInfo])(implicit modifier: Modifier): Future[ConcreteRunnable] =
    createRunnableAndGetId(create, recoverInfo)
      .flatMap(getRunnable)

  def startRunnable(runnableId: Runnable.Id, recoverInfo: Option[RecoverInfo])(implicit modifier: Modifier): Future[Info] =
    ask(managerActor, StartRunnableCmd(runnableId, recoverInfo, modifier))
      .mapTo[RunnableInfoOpt]
      .map(_.x)
      .map(valueOrNotFound(runnableId))
      .mapTo[Info]

  def startRunnableAndGetId(runnableId: Runnable.Id, recoverInfo: Option[RecoverInfo])(implicit modifier: Modifier): Future[Runnable.Id] =
    startRunnable(runnableId, recoverInfo).map(_.id)

  def updateRunnable(update: UpdateRunnableInfo)(implicit modifier: Modifier): Future[Runnable.Id] =
    ask(managerActor, UpdateRunnableCmd(update, modifier))
      .mapTo[Response]
      .map(idOrNotFound(update.id))

  def deleteRunnable(runnableId: Runnable.Id)(implicit modifier: Modifier): Future[Runnable.Id] =
    ask(managerActor, DeleteRunnableCmd(runnableId, modifier))
      .mapTo[Response]
      .map(idOrNotFound(runnableId))

  def completeRunnable(runnableId: Runnable.Id, recoverInfo: Option[RecoverInfo])(implicit modifier: Modifier): Unit =
    managerActor ! CompleteRunnableCmd(runnableId, recoverInfo, modifier)

  def getRunnableHistoryData(runnableId: Runnable.Id): Future[ConcreteRunnableHistoryData] =
    historyManager
      .getHistoryData(runnableId)
      .mapTo[ConcreteRunnableHistoryData]

  def getRunnableHistoryDataMap: Future[Map[HistoryData.Id, HistoryData]] =
    historyManager
      .getHistoryDataAll
      .mapTo[Map[HistoryData.Id, HistoryData]]
}

trait RunnableManagerBehavior extends CqrsPersistentActorLike
  with NodeContainerBehavior
  with RunnableCreator
  with RunnableManagerTypes
  with Printable {

  def bpm: Bpm
  def managerType: RunnableManagerType

  override val supervisorStrategy: OneForOneStrategy = {
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case e: BpmException =>
        log.error(e, e.getMessage)
        Resume
      case e =>
        super.supervisorStrategy.decider.applyOrElse(e, (_: Any) => Escalate)
    }
  }

  def getRunnableParent(parentId: Runnable.Id): Runnable = new Runnable(getChild(parentId))

  def getRunnable(info: RunnableInfo): Future[Runnable] = {
    info.parentId match {
      case Some(x) => getRunnableParent(x).getRunnable(info.id)
      case None => Future.successful(new Runnable(getChild(info.id)))
    }
  }

  def getRunnableById(state: State, runnableId: Runnable.Id): Future[Runnable] = {
    getRunnable(state.getRunnableInfo(runnableId))
  }

  def getRunnableOpt(state: State, runnableId: Runnable.Id): Future[Option[Runnable]] = {
    state.getRunnableInfoOpt(runnableId) match {
      case Some(info) => info.parentId match {
        case Some(parentId) => getRunnableParent(parentId).getRunnableOpt(info.id)
        case None => Future.successful(getChildOpt(info.id).map(new Runnable(_)))
      }
      case None => Future.successful(None)
    }
  }

  def getRunnableRefOpt(state: State, runnableId: Runnable.Id): Future[Option[ActorRef]] = {
    getRunnableOpt(state, runnableId).map(_.map(_.actorRef))
  }

  def logRunnableStatus(info: RunnableInfo, status: RunnableStatus): Unit = {
    log.info(s"${info.runnableType} ${status.statusType.blue} [${info.id.name}]")
  }

  def createRunnable(state: State, info: RunnableInfo, props: Props)(implicit modifier: Modifier): Unit = {
    persist(state, CreatedRunnableInfoEvt(info)) { (state, event) =>
      info.parentId match {
        case Some(parentId) =>
          state.getRunnableInfoOpt(parentId).fold(sender() ! RunnableNotFound(parentId)) { parentInfo =>
            val childIds = Some(parentInfo.childIds + info.id)
            val update = managerType match {
              case ProjectManagerType => UpdateProjectInfo(id = parentId, childIds = childIds)
              case ProcessManagerType => UpdateProcessInfo(id = parentId, childIds = childIds)
              case TaskManagerType => UpdateTaskInfo(id = parentId, childIds = childIds)
            }

            persist(state, UpdatedRunnableInfoEvt(update, modifier)) { (state, event) =>
              after(getRunnableParent(parentId).createSubRunnable(props, info.id.name)) { (response, state) =>
                publish(event)
                sender() ! Done
                logRunnableStatus(info, RunnableStatus.Created)
              }
            }
          }
        case None =>
          context.actorOf(props = props, name = info.id.name)
          publish(event)
          sender() ! Done
          logRunnableStatus(info, RunnableStatus.Created)
      }
    }
  }

  def preStartRunnable(state: State, runnableId: Runnable.Id)(implicit modifier: Modifier): Unit = {
    def start(state: State, info: RunnableInfo): Unit = {
      val update = managerType match {
        case ProjectManagerType => UpdateProjectInfo(
          id = runnableId,
          status = Option(RunnableStatus.Started),
          startedBy = Option(Option(modifier)),
          startedOn = Option(Option(ZonedDateTime.now())))
        case ProcessManagerType => UpdateProcessInfo(
          id = runnableId,
          status = Option(RunnableStatus.Started),
          startedBy = Option(Option(modifier)),
          startedOn = Option(Option(ZonedDateTime.now())))
        case TaskManagerType => UpdateTaskInfo(
          id = runnableId,
          status = Option(RunnableStatus.Started),
          startedBy = Option(Option(modifier)),
          startedOn = Option(Option(ZonedDateTime.now())))
      }

      persist(state, UpdatedRunnableInfoEvt(update, modifier)) { (state, event) =>
        after(getRunnable(info).flatMap(_.start())) { (response, state) =>
          publish(event)
          getRunnableInfoById(state, runnableId)
          logRunnableStatus(info, RunnableStatus.Started)
        }
      }
    }

    foldParent(state, runnableId, start) { (state, info, parentInfo, parentId) =>
      if (parentInfo.activeChildIds contains parentId) sender() ! AlreadyStarted(runnableId) else {
        val activeChildIds = Some(parentInfo.childIds + runnableId)
        val update = managerType match {
          case ProjectManagerType => UpdateProjectInfo(id = parentId, activeChildIds = activeChildIds)
          case ProcessManagerType => UpdateProcessInfo(id = parentId, activeChildIds = activeChildIds)
          case TaskManagerType => UpdateTaskInfo(id = parentId, activeChildIds = activeChildIds)
        }

        persist(state, UpdatedRunnableInfoEvt(update, modifier)) { (state, event) =>
          start(state, info)
        }
      }
    }
  }

  def startRunnable(state: State, runnableId: Runnable.Id, recoverInfo: Option[RecoverInfo])(implicit modifier: Modifier): Unit = {
    /**
     * Recover start
     */
    recoverInfo match {
      case Some(x) =>
        val runnableType = x.runnableType
        log.warning(s"trying recover start $runnableType [${runnableType}Id: $runnableId]")
        val info = state.getRunnableInfo(runnableId)
        info.status match {
          case RunnableStatus.Created =>
            log.warning(s"trying start $runnableType [${runnableType}Id: $runnableId]")
            preStartRunnable(state, runnableId)
          case RunnableStatus.Started =>
            log.warning(s"trying restart $runnableType [${runnableType}Id: $runnableId]")
            preStartRunnable(state, runnableId)
          case RunnableStatus.WaitingComplete =>
            info match {
              case x: ProjectInfo =>
              case x: ProcessInfo =>
              case x: TaskInfo if x.taskType == TaskType.ScriptTask =>
                log.warning(s"trying restart ScriptTask [${runnableType}Id: $runnableId]")
                preStartRunnable(state, runnableId)
              case x: TaskInfo if x.taskType == TaskType.SendTask =>
                log.warning(s"trying restart SendTask [${runnableType}Id: $runnableId]")
                preStartRunnable(state, runnableId)
              case _ =>
            }
          case _ =>
            log.warning(s"$runnableType already started [${runnableType}Id: $runnableId]")
        }
      case None => preStartRunnable(state, runnableId)
    }
  }

  def foldParent(state: State, runnableId: Runnable.Id, ifEmpty: (State, RunnableInfo) => Unit)(f: (State, RunnableInfo, RunnableInfo, Runnable.Id) => Unit): Unit = {
    state.getRunnableInfoOpt(runnableId).fold(sender() ! RunnableNotFound(runnableId)) { info =>
      info.parentId.fold(ifEmpty(state, info)) { parentId =>
        state.getRunnableInfoOpt(parentId).fold(sender() ! RunnableNotFound(parentId)) { parentInfo =>
          f(state, info, parentInfo, parentId)
        }
      }
    }
  }

  def preCompleteRunnable(state: State, runnableId: Runnable.Id)(implicit modifier: Modifier): Unit = {
    def complete(state: State, info: RunnableInfo): Unit = {
      // for task we must notify operation
      def notifyOperation = info match {
        case x: TaskInfo => x.relaredOperationId match {
          case Some(operationId) => bpm.completeOperation(x)
          case None => Future.successful(info)
        }
        case x => Future.successful(info)
      }

      def runnableCompleted = notifyOperation
        .flatMap(getRunnable)
        .flatMap(_.complete(info))

      persistAfter(runnableCompleted, DeletedRunnableInfoEvt(runnableId)) { (response, state, event) =>
        publish(event)
        logRunnableStatus(info, RunnableStatus.Completed)
      }
    }

    foldParent(state, runnableId, complete) { (state, info, parentInfo, parentId) =>
      if (!(parentInfo.activeChildIds contains runnableId)) sender() ! NotStarted(runnableId) else {
        val activeChildIds = Some(parentInfo.childIds - runnableId)
        val update = managerType match {
          case ProjectManagerType => UpdateProjectInfo(id = parentId, activeChildIds = activeChildIds)
          case ProcessManagerType => UpdateProcessInfo(id = parentId, activeChildIds = activeChildIds)
          case TaskManagerType => UpdateTaskInfo(id = parentId, activeChildIds = activeChildIds)
        }

        persist(state, UpdatedRunnableInfoEvt(update, modifier)) { (state, event) =>
          complete(state, info)
        }
      }
    }
  }

  def completeRunnable(state: State, runnableId: Runnable.Id, recoverInfo: Option[RecoverInfo])(implicit modifier: Modifier): Unit = {
    /**
     * Recover complete
     */
    recoverInfo match {
      case Some(x) =>
        val runnableType = x.runnableType
        log.warning(s"trying recover $runnableType complete [${runnableType}Id: $runnableId]")
        state.getRunnableInfoOpt(runnableId) match {
          case Some(info) =>
            info.status match {
              case RunnableStatus.Started | RunnableStatus.WaitingComplete =>
                log.warning(s"trying complete $runnableType [${runnableType}Id: $runnableId]")
                preCompleteRunnable(state, runnableId)
              case RunnableStatus.Completed =>
                log.warning(s"trying recomplete $runnableType [${runnableType}Id: $runnableId]")
                preCompleteRunnable(state, runnableId)
            }
          case None =>
            log.warning(s"$runnableType already completed [${runnableType}Id: $runnableId]")
        }
      case None => preCompleteRunnable(state, runnableId)
    }
  }

  def updateRunnable(state: State, update: UpdateRunnableInfo)(implicit modifier: Modifier): Unit = {
    if (state.runnableInfoExists(update.id)) {
      persist(state, UpdatedRunnableInfoEvt(update, modifier)) { (state, event) =>
        sender() ! Done
      }
    } else sender() ! RunnableNotFound
  }

  def deleteRunnable(state: State, runnableId: Runnable.Id)(implicit modifier: Modifier): Unit = {
    def delete(state: State, info: RunnableInfo): Unit = {
      val x = info.childIds.flatMap(x => state.getRunnableInfoOpt(x))
      x.foreach(x => deleteRunnable(state, x.id))
      val f1 = Future.sequence(x.map(x => getRunnable(x).flatMap(_.delete(x))))
      val f2 = getRunnable(info).flatMap(_.delete(info))

      persistAfter(f1 zip f2, DeletedRunnableInfoEvt(runnableId)) { (response, state, event) =>
        publish(event)
        sender() ! Done
      }
    }

    foldParent(state, runnableId, delete) { (state, info, parentInfo, parentId) =>
      val activeChildIds = Some(parentInfo.childIds - runnableId)
      val update = managerType match {
        case ProjectManagerType => UpdateProjectInfo(id = parentId, activeChildIds = activeChildIds)
        case ProcessManagerType => UpdateProcessInfo(id = parentId, activeChildIds = activeChildIds)
        case TaskManagerType => UpdateTaskInfo(id = parentId, activeChildIds = activeChildIds)
      }

      persist(state, UpdatedRunnableInfoEvt(update, modifier)) { (state, event) =>
        delete(state, info)
      }
    }
  }

  def getRunnableInfoById(state: State, runnableId: Runnable.Id): Unit = {
    sender() ! RunnableInfoOpt(state.getRunnableInfoOpt(runnableId))
  }

  def getRunnableInfoMap(state: State): Unit = {
    sender() ! RunnableInfoMap(state.storage)
  }

  def getRunnableExtendedInfo(state: State, runnableId: Runnable.Id): Unit = {
    state.getRunnableInfoOpt(runnableId).fold(sender() ! RunnableExtendedInfoOpt(None)) { x =>
      getRunnable(x).flatMap(_.getExtendedInfoOpt(x)) pipeTo sender()
    }
  }

  def getRunnableExtendedInfoMap(state: State): Unit = {
    def extendedInfoOptSet = state.getRunnableInfoSet.map { info =>
      getRunnable(info).flatMap(_.getExtendedInfoOpt(info))
    }
    def extendedInfoMap = Future.sequence(extendedInfoOptSet).map {
      _.flatMap(_.x)
        .map(x => x.info.id -> x)
        .toMap
        .map(x => x._1.raw -> x._2)
    }
    extendedInfoMap.map(RunnableExtendedInfoMap) pipeTo sender()
  }

  def getRunnableStatus(state: State, runnableId: Runnable.Id): Unit = {
    sender() ! RunnableStatusOpt(state.getRunnableInfoOpt(runnableId).map(_.status))
  }

  def forward(state: State, x: Forward): Unit = {
    getRunnableRefOpt(state, x.runnableId).foreach { runnableOpt =>
      runnableOpt foreach (_ forward x)
    }
  }

  def changeRunnableStatus(state: State, runnableId: Runnable.Id, status: RunnableStatus, modifier: Modifier): Unit = {
    persistOnly(state, ChangedStatusEvt(runnableId, status, modifier))
  }

  def behavior(state: State): Receive = {
    case CreateRunnableCmd(x, y, z) => preCreateRunnable(state, x, y, sender(), z)
    case RunnableCreator.CreateRunnableCmd(x, y, z) => createRunnable(state, x, y)(z)
    case StartRunnableCmd(x, y, z) => startRunnable(state, x, y)(z)
    case ChangeStatusCmd(x, y, z) => changeRunnableStatus(state, x, y, z)
    case CompleteRunnableCmd(x, y, z) => completeRunnable(state, x, y)(z)
    case UpdateRunnableCmd(x, y) => updateRunnable(state, x)(y)
    case DeleteRunnableCmd(x, y) => deleteRunnable(state, x)(y)
    case GetRunnableInfoById(x) => getRunnableInfoById(state, x)
    case GetRunnableInfoMap => getRunnableInfoMap(state)
    case GetRunnableExtendedInfo(x) => getRunnableExtendedInfo(state, x)
    case GetRunnableExtendedInfoMap => getRunnableExtendedInfoMap(state)
    case GetRunnableStatus(x) => getRunnableStatus(state, x)
    case x: Forward => forward(state, x)
  }

  override def afterRecover(state: State): Unit = {
    def createChild(state: State, info: RunnableInfo): Unit = {
      val name = info.id.name
      context.actorOf(props = props(info, bpm), name = name)
      log.info(s"Runnable [$name] recovered")
    }

    state.storage.values.foreach { info =>
      foldParent(state, info.id, createChild) { (state, info, parentInfo, parentId) =>
        getRunnableParent(parentId).createSubRunnable(props(info, bpm), info.id.name)
      }
    }
  }
}

trait RunnableManagerState extends CqrsState with RunnableManagerStateTypes {
  val storage: Map[RawId, Info]

  def withStorage(storage: Map[RawId, Info]): RunnableManagerState = {
    (this, storage) match {
      case (x: ProjectManagerState, y: Map[RawId, ProjectInfo] @unchecked) => x.copy(y)
      case (x: ProcessManagerState, y: Map[RawId, ProcessInfo] @unchecked) => x.copy(y)
      case (x: TaskManagerState, y: Map[RawId, TaskInfo] @unchecked) => x.copy(y)
      case _ => sys.error("Invalid RunnableManagerState")
    }
  }

  def createRunnableInfo(x: Info): RunnableManagerState = withStorage(storage + (x.id.raw -> x))

  def updateRunnableInfo(p1: UpdateInfo, p2: Modifier): RunnableManagerState = {
    val x1 = storage(p1.id.raw)

    val modifyAttr = x1.modifyAttr.copy(
      updatedBy = Option(p2),
      updatedOn = Option(ZonedDateTime.now()))

    val x3 = (x1, p1) match {
      case (x: ProjectInfo, y: UpdateProjectInfo) => ProjectInfo(
        id = x.id,
        name = y.name.getOrElse(x.name),
        description = y.description.getOrElse(x.description),
        status = y.status.getOrElse(x.status),
        parentId = x.parentId,
        childIds = y.childIds.getOrElse(x.childIds),
        activeChildIds = y.activeChildIds.getOrElse(x.activeChildIds),
        templateId = x.templateId,
        managerId = y.managerId.getOrElse(x.managerId),
        relaredOperationId = x.relaredOperationId,
        relatedOperationBpmnId = x.relatedOperationBpmnId,
        startedBy = y.startedBy.getOrElse(x.startedBy),
        startedOn = y.startedOn.getOrElse(x.startedOn),
        modifyAttr = modifyAttr)
      case (x: ProcessInfo, y: UpdateProcessInfo) => ProcessInfo(
        id = x.id,
        name = y.name.getOrElse(x.name),
        description = y.description.getOrElse(x.description),
        status = y.status.getOrElse(x.status),
        bpmnId = x.bpmnId,
        parentId = x.parentId,
        childIds = y.childIds.getOrElse(x.childIds),
        activeChildIds = y.activeChildIds.getOrElse(x.activeChildIds),
        projectId = x.projectId,
        initiatorId = x.initiatorId,
        templateId = x.templateId,
        managerId = y.managerId.getOrElse(x.managerId),
        operationIds = y.operationIds.getOrElse(x.operationIds),
        relaredOperationId = x.relaredOperationId,
        relatedOperationBpmnId = x.relatedOperationBpmnId,
        startedBy = y.startedBy.getOrElse(x.startedBy),
        startedOn = y.startedOn.getOrElse(x.startedOn),
        modifyAttr = modifyAttr)
      case (x: TaskInfo, y: UpdateTaskInfo) => TaskInfo(
        id = y.id,
        name = y.name.getOrElse(x.name),
        description = y.description.getOrElse(x.description),
        taskType = x.taskType,
        status = y.status.getOrElse(x.status),
        initiator = y.initiator.getOrElse(x.initiator),
        ownerId = x.ownerId,
        assigneeId = y.assigneeId.getOrElse(x.assigneeId),
        reviewerId = y.reviewerId.getOrElse(x.reviewerId),
        watcherIds = y.watcherIds.getOrElse(x.watcherIds),
        parentId = x.parentId,
        childIds = y.childIds.getOrElse(x.childIds),
        activeChildIds = y.activeChildIds.getOrElse(x.activeChildIds),
        processId = x.processId,
        subProcessId = x.subProcessId,
        relaredOperationId = x.relaredOperationId,
        relatedOperationBpmnId = x.relatedOperationBpmnId,
        formTemplateId = x.formTemplateId,
        startedBy = y.startedBy.getOrElse(x.startedBy),
        startedOn = y.startedOn.getOrElse(x.startedOn),
        actualEnd = y.actualEnd.getOrElse(x.actualEnd),
        plannedStart = x.plannedStart,
        plannedDuration = x.plannedDuration,
        modifyAttr = modifyAttr)
      case _ => sys.error("Invalid RunnableInfo")
    }

    withStorage(storage + (x1.id.raw -> x3.asInstanceOf[Info]))
  }

  def deleteRunnableInfo(runnableId: Runnable.Id): RunnableManagerState = withStorage(storage - runnableId.raw)

  def runnableInfoExists(runnableId: Runnable.Id): Boolean = storage.exists(_._1 == runnableId.raw)

  def runnableInfoExists(runnableId: Runnable.RawId): Boolean = storage.exists(_._1 == runnableId)

  def getRunnableInfo(runnableId: Runnable.Id): Info = storage(runnableId.raw)

  def getRunnableInfoOpt(runnableId: Runnable.Id): Option[Info] = storage.get(runnableId.raw)

  def getRunnableInfoSet: Set[Info] = storage.values.toSet

  def getRunnableInfoByOperationMap: Map[Operation.Id, RunnableInfo] = {
    storage.values
      .flatMap(x => x.relaredOperationId.map(_ -> x))
      .toMap
  }

  def getRunnableInfoByOperation(operationId: Operation.Id): Option[RunnableInfo] = {
    getRunnableInfoByOperationMap.get(operationId)
  }

  def changeRunnableStatus(runnableId: Runnable.Id, status: RunnableStatus, modifier: Modifier): RunnableManagerState = {
    val x1 = getRunnableInfo(runnableId)

    val modifyAttr = x1.modifyAttr.copy(
      updatedBy = Option(modifier),
      updatedOn = Option(ZonedDateTime.now()))

    val x2 = x1 match {
      case x: ProjectInfo => x.copy(status = status, modifyAttr = modifyAttr)
      case x: ProcessInfo => x.copy(status = status, modifyAttr = modifyAttr)
      case x: TaskInfo => x.copy(status = status, modifyAttr = modifyAttr)
    }
    withStorage(storage + (runnableId.raw -> x2.asInstanceOf[Info]))
  }

  def update: Update = {
    case CreatedRunnableInfoEvt(x: Info) => createRunnableInfo(x)
    case UpdatedRunnableInfoEvt(x: UpdateInfo, y) => updateRunnableInfo(x, y)
    case DeletedRunnableInfoEvt(x) => deleteRunnableInfo(x)
    case ChangedStatusEvt(x, y, z) => changeRunnableStatus(x, y, z)
  }
}

trait RunnableManagerType

object RunnableManagerType {
  val ProjectManagerTypeName = "projectManager"
  val ProcessManagerTypeName = "processManager"
  val TaskManagerTypeName = "taskManager"

  case object ProjectManagerType extends RunnableManagerType
  case object ProcessManagerType extends RunnableManagerType
  case object TaskManagerType extends RunnableManagerType
}

object RunnableManager {
  type Id = ActorId

  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait CreateRunnableResponse extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateRunnableCmd(create: CreateRunnableInfo, recoverInfo: Option[RecoverInfo], modifier: Modifier) extends Command
  case class StartRunnableCmd(runnableId: Runnable.Id, recoverInfo: Option[RecoverInfo], modifier: Modifier) extends Command
  case class CompleteRunnableCmd(runnableId: Runnable.Id, recoverInfo: Option[RecoverInfo], modifier: Modifier) extends Command
  case class UpdateRunnableCmd(update: UpdateRunnableInfo, modifier: Modifier) extends Command
  case class DeleteRunnableCmd(runnableId: Runnable.Id, modifier: Modifier) extends Command
  case class ChangeStatusCmd(runnableId: Runnable.Id, status: RunnableStatus, modifier: Modifier) extends Command

  case class GetRunnableInfoById(runnableId: Runnable.Id) extends Request
  case object GetRunnableInfoMap extends Request
  case class GetRunnableExtendedInfo(runnableId: Runnable.Id) extends Request
  case object GetRunnableExtendedInfoMap extends Request
  case class GetRunnableStatus(runnableId: Runnable.Id) extends Request
  case class Forward(runnableId: Runnable.Id, childId: ActorId, message: Any) extends Request
  case class ForwardWithReply(runnableId: Runnable.Id, childId: ActorId, message: Any) extends Request

  case class CreatedRunnableInfoEvt(info: RunnableInfo) extends Event
  case class UpdatedRunnableInfoEvt(update: UpdateRunnableInfo, modifier: Modifier) extends Event
  case class DeletedRunnableInfoEvt(runnableId: Runnable.Id) extends Event
  case class ChangedStatusEvt(runnableId: Runnable.Id, status: RunnableStatus, modifier: Modifier) extends Event

  case object Done extends Response
  case class RunnableNotFound(runnableId: Runnable.Id) extends Response
  case class AlreadyStarted(runnableId: Runnable.Id) extends Response
  case class NotStarted(runnableId: Runnable.Id) extends Response
  case class CreateRunnableSuccess(x: RunnableInfo) extends CreateRunnableResponse
  case class CreateRunnableFailure(x: List[BpmException]) extends CreateRunnableResponse
  case class RunnableInfoOpt(x: Option[RunnableInfo]) extends Response
  case class RunnableInfoMap(x: Map[RawId, RunnableInfo]) extends Response
  case class RunnableExtendedInfoOpt(x: Option[RunnableExtendedInfo]) extends Response
  case class RunnableExtendedInfoMap(x: Map[RawId, RunnableExtendedInfo]) extends Response
  case class RunnableStatusOpt(x: Option[RunnableStatus]) extends Response

  def props(info: RunnableInfo, bpm: Bpm)(implicit c: ExecutionContext, t: Timeout): Props = {
    info match {
      case x: ProjectInfo => ProjectActor.props(
        projectId = x.id,
        bpm = bpm)

      case x: ProcessInfo => ProcessActor.props(
        processId = x.id,
        projectId = x.projectId,
        bpm = bpm)

      case x: TaskInfo => x.taskType match {
        case TaskType.UserTask => UserTaskActor.props(
          taskId = x.id,
          bpm = bpm)
        case TaskType.ScriptTask => ScriptTaskActor.props(
          taskId = x.id,
          bpm = bpm)
        case TaskType.SendTask => SendTaskActor.props(
          taskId = x.id,
          bpm = bpm)
        case TaskType.SubProcessTask => SubProcessTaskActor.props(
          taskId = x.id,
          processId = x.processId.get,
          subProcessId = x.subProcessId.get,
          bpm = bpm)
        case _ => sys.error("Invalid task type")
      }
    }
  }
}

