package sphera.bpm.runnable

import java.time.ZonedDateTime

import akka.actor.SupervisorStrategy.{ Escalate, Resume }
import akka.actor.{ ActorRef, OneForOneStrategy, Props }
import akka.pattern.AskSupport
import akka.util.Timeout
import sphera.bpm.Implicits._
import sphera.bpm.history.HistoryManager
import sphera.bpm.history.actor.HistoryManagerActor
import sphera.bpm.history.model.HistoryData
import sphera.bpm.masterdata.model.DataStructure
import sphera.bpm.model.{ ImplicitModifier, Modifier, ModifyAttr }
import sphera.bpm.process.actor.ProcessState
import sphera.bpm.process.{ CreateProcessInfo, Operation, ProcessExtendedInfo, ProcessInfo }
import sphera.bpm.project.actor.ProjectState
import sphera.bpm.project.{ CreateProjectInfo, ProjectExtendedInfo, ProjectInfo }
import sphera.bpm.runnable.Runnable._
import sphera.bpm.runnable.RunnableManager.{ ChangeStatusCmd, Forward, ForwardWithReply, RunnableExtendedInfoOpt }
import sphera.bpm.storage.definition.{ DefStorage, Description }
import sphera.bpm.storage.exception.ExceptionStorage
import sphera.bpm.storage.runnable.RunnableStorage
import sphera.bpm.storage.runnable.actor.{ RunnableStorageNodeBehavior, RunnableStorageNodeStateLike }
import sphera.bpm.task._
import sphera.bpm.task.actor._
import sphera.bpm.{ Bpm, BpmException, RunnableNotFoundException }
import sphera.core.akkaext.actor.CqrsRequest.GetChild
import sphera.core.akkaext.actor.CqrsResponse.ChildOpt
import sphera.core.akkaext.actor._
import sphera.core.akkaext.persistence.CqrsPersistentActorLike
import com.outworkers.phantom.dsl.UUID

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.classTag
import scala.reflect.runtime.universe._

trait RunnableLike extends AskSupport with RunnableTypes with FutureSupport {
  def actorRef: ActorRef

  def getState: Future[State] = actorRef.ask(GetState).mapTo[StateRes].map(_.x).mapTo[State]

  def getExtendedInfo(info: Info): Future[ExtendedInfo] = getState.map(RunnableExtendedInfo(info, _)).mapTo[ExtendedInfo]

  def getExtendedInfoOpt(info: Info): Future[RunnableExtendedInfoOpt] = getExtendedInfo(info).map { x => RunnableExtendedInfoOpt(Some(x)) }

  def getRunnable(runnableId: Runnable.Id): Future[Runnable] =
    actorRef.ask(GetChild(runnableId))
      .mapTo[ChildOpt]
      .map(_.actorRef)
      .map {
        case Some(x) => new Runnable(x)
        case None => throw RunnableNotFoundException(runnableId)
      }

  def getRunnableOpt(runnableId: Runnable.Id): Future[Option[Runnable]] =
    actorRef.ask(GetChild(runnableId))
      .mapTo[ChildOpt]
      .map(_.actorRef.map(new Runnable(_)))

  def start(): Future[Response] = actorRef.ask(StartCmd).mapTo[Response]

  def complete(info: Info)(implicit modifier: Modifier): Future[Response] = actorRef.ask(CompleteCmd(info, modifier)).mapTo[Response]

  def delete(info: Info)(implicit modifier: Modifier): Future[Response] = actorRef.ask(DeleteCmd(info, modifier)).mapTo[Response]

  def createSubRunnable(props: Props, name: String): Future[ActorRef] = actorRef.ask(props -> name).mapTo[ActorRef]
}

class Runnable(val actorRef: ActorRef)(implicit val c: ExecutionContext, val t: Timeout) extends RunnableLike {
  type State = RunnableState
  type Info = RunnableInfo
  type CreateInfo = CreateRunnableInfo
  type UpdateInfo = UpdateRunnableInfo
  type ExtendedInfo = RunnableExtendedInfo

  val ctag1 = classTag[Info]
  val ttag1 = typeTag[Info]
  val ctag2 = classTag[CreateInfo]
  val ctag3 = classTag[UpdateInfo]
  val ctag4 = classTag[ExtendedInfo]
  val ctag7 = classTag[State]
}

object Runnable {
  type Id = ActorId
  type RawId = String

  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case object StartCmd extends Command
  case class CompleteCmd(info: RunnableInfo, modifier: Modifier) extends Command
  case class DeleteCmd(info: RunnableInfo, modifier: Modifier) extends Command

  case object GetState extends Request

  case object Started extends Response
  case object Completed extends Response
  case object Deleted extends Response
  case class StateRes(x: RunnableState) extends Response
  case object ChildNotFound extends Response
}

trait RunnableState extends RunnableStorageNodeStateLike {
  def storage: RunnableStorage

  def withDefStorage(defStorage: DefStorage): RunnableState = this match {
    case x: ProcessState => x.copy(storage = x.storage.copy(defStorage = defStorage))
    case x: ProjectState => x.copy(storage = x.storage.copy(defStorage = defStorage))
    case x: UserTaskState => x.copy(storage = x.storage.copy(defStorage = defStorage))
    case x: SendTaskState => x.copy(storage = x.storage.copy(defStorage = defStorage))
    case x: ScriptTaskState => x.copy(storage = x.storage.copy(defStorage = defStorage))
    case x: SubProcessTaskState => x.copy(storage = x.storage.copy(defStorage = defStorage))
  }

  def withExceptionStorage(exceptionStorage: ExceptionStorage): RunnableState = this match {
    case x: ProcessState => x.copy(storage = x.storage.copy(exceptionStorage = exceptionStorage))
    case x: ProjectState => x.copy(storage = x.storage.copy(exceptionStorage = exceptionStorage))
    case x: UserTaskState => x.copy(storage = x.storage.copy(exceptionStorage = exceptionStorage))
    case x: SendTaskState => x.copy(storage = x.storage.copy(exceptionStorage = exceptionStorage))
    case x: ScriptTaskState => x.copy(storage = x.storage.copy(exceptionStorage = exceptionStorage))
    case x: SubProcessTaskState => x.copy(storage = x.storage.copy(exceptionStorage = exceptionStorage))
  }

  def withRunnableStorage(storage: RunnableStorage): RunnableState = this match {
    case x: ProcessState => x.copy(storage = storage)
    case x: ProjectState => x.copy(storage = storage)
    case x: UserTaskState => x.copy(storage = storage)
    case x: SendTaskState => x.copy(storage = storage)
    case x: ScriptTaskState => x.copy(storage = storage)
    case x: SubProcessTaskState => x.copy(storage = storage)
  }
}

trait RunnableInfo {
  def id: Runnable.Id
  def parentId: Option[Runnable.Id]
  def childIds: Set[Runnable.Id]
  def activeChildIds: Set[Runnable.Id]
  def relaredOperationId: Option[Operation.Id]
  def relatedOperationBpmnId: Option[Operation.BpmnId]
  def modifyAttr: ModifyAttr
  def status: RunnableStatus

  def withStatus(status: RunnableStatus, modifyAttr: ModifyAttr): RunnableInfo = {
    this match {
      case x: ProjectInfo => x.copy(status = status, modifyAttr = modifyAttr)
      case x: ProcessInfo => x.copy(status = status, modifyAttr = modifyAttr)
      case x: TaskInfo => x.copy(status = status, modifyAttr = modifyAttr)
    }
  }

  val runnableType: String = this match {
    case x: ProjectInfo => RunnableType.Project
    case x: ProcessInfo if x.parentId.isEmpty => RunnableType.Process
    case x: ProcessInfo => RunnableType.SubProcess
    case x: TaskInfo => RunnableType.Task
  }
}

trait RunnableExtendedInfo {
  def info: RunnableInfo
  def state: RunnableState
}

object RunnableExtendedInfo {
  def apply(info: RunnableInfo, state: RunnableState): RunnableExtendedInfo = {
    (info, state) match {
      case (x: ProjectInfo, y: ProjectState) => ProjectExtendedInfo(x, y)
      case (x: ProcessInfo, y: ProcessState) => ProcessExtendedInfo(x, y)
      case (x: TaskInfo, y: TaskState) => TaskExtendedInfo(x, y)
      case _ => sys.error("Invalid RunnableExtendedInfo")
    }
  }
}

case class RecoverInfo(runnableType: String)

sealed trait RunnableStatus {
  def statusType = this match {
    case RunnableStatus.Created => StatusType.Created
    case RunnableStatus.Started => StatusType.Started
    case RunnableStatus.Deleted => StatusType.Deleted
    case RunnableStatus.Completed => StatusType.Completed
    case RunnableStatus.WaitingComplete => StatusType.WaitingComplete
  }
}

object StatusType {
  val Created = "created"
  val Started = "started"
  val Deleted = "deleted"
  val Completed = "completed"
  val WaitingComplete = "waiting"
}

object RunnableStatus {
  case object Created extends RunnableStatus
  case object Started extends RunnableStatus
  case object WaitingComplete extends RunnableStatus
  case object Completed extends RunnableStatus
  case object Deleted extends RunnableStatus
  case object Failed extends RunnableStatus
}

trait RunnableTemplate {
  def id: RunnableTemplate.Id
  def dataStructureId: DataStructure.Id
}

object RunnableTemplate {
  type Id = UUID
}

trait CreateRunnableInfo {
  def name: String
  def description: Description
  //def data: Json
  def parentId: Option[Runnable.Id]
  def relatedOperationId: Option[Operation.Id]
  def relatedOperationBpmnId: Option[Operation.BpmnId]

  val runnableType: String = this match {
    case x: CreateProjectInfo => RunnableType.Project
    case x: CreateProcessInfo if x.parentId.isEmpty => RunnableType.Process
    case x: CreateProcessInfo => RunnableType.SubProcess
    case x: CreateTaskInfo => RunnableType.Task
  }
}

sealed trait RunnableType

object RunnableType {
  val Project = "project"
  val Process = "process"
  val SubProcess = "subProcess"
  val Task = "task"
}

trait UpdateRunnableInfo {
  def id: Runnable.Id
  def status: Option[RunnableStatus]
  def childIds: Option[Set[Runnable.Id]]
  def activeChildIds: Option[Set[Runnable.Id]]
  def startedBy: Option[Option[Modifier]]
  def startedOn: Option[Option[ZonedDateTime]]
}

trait RunnableHistoryData

trait RunnableBehavior extends CqrsPersistentActorLike
  with RunnableStorageNodeBehavior
  with FutureSupport
  with ImplicitModifier
  with RunnableTypes {

  def bpm: Bpm
  def historyManager: HistoryManager

  override val supervisorStrategy: OneForOneStrategy = {
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case e: BpmException =>
        log.error(e, e.getMessage)
        Resume
      case e =>
        super.supervisorStrategy.decider.applyOrElse(e, (_: Any) => Escalate)
    }
  }

  def preStart(state: State): Unit = {
    sender() ! Started
    log.info("started")
    parent ! ChangeStatusCmd(id, RunnableStatus.WaitingComplete, modifier)
    start(state)
  }

  def start(state: State): Unit

  def createHistoryData(info: Info, state: State, status: RunnableStatus, modifier: Modifier): HistoryData = {
    val x1 = info.modifyAttr.copy(updatedBy = Some(modifier), updatedOn = Some(ZonedDateTime.now()))
    val x2 = info.withStatus(status, x1)
    val x3 = HistoryData(
      historyDataId = info.id,
      extendedInfo = RunnableExtendedInfo(x2, state),
      completedOn = Some(ZonedDateTime.now()))
    x3
  }

  def complete(state: State, info: Info, modifier: Modifier): Unit = {
    val historyData = createHistoryData(
      info = info,
      state = state,
      status = RunnableStatus.Completed,
      modifier = modifier)

    // for script task we don't create history
    val createHistory = info match {
      case x: TaskInfo if x.taskType == TaskType.ScriptTask => Future.successful(HistoryManagerActor.Done)
      case x => historyManager.createHistoryData(historyData)
    }

    after(createHistory) { (response, state) =>
      info match {
        case x: ProcessInfo =>
          after(bpm.getTasksByProcessId(x.id)) { (response, event) =>
            response foreach (taskId => log.warning(s"has running task: $taskId"))
          }
        case x: TaskInfo =>
        case x =>
      }

      sender() ! Completed
      context.stop(self)
      log.info("completed")
    }
  }

  def delete(state: State, info: Info, modifier: Modifier): Unit = {
    info match {
      case x: ProcessInfo => bpm.taskManager.deleteTasksByProcessId(id)
      case x =>
    }

    after(historyManager.createHistoryData(createHistoryData(
      info = info,
      state = state,
      status = RunnableStatus.Deleted,
      modifier = modifier))) { (response, state) =>
      sender() ! Deleted
      context.stop(self)
      log.info("deleted")
    }
  }

  def forward(state: State, x: Forward): Unit = {
    getChildOpt(x.childId).fold(sender() ! ChildNotFound)(_ forward x.message)
  }

  def forwardWithReply(state: State, x: ForwardWithReply): Unit = {
    getChildOpt(x.childId).fold(sender() ! ChildNotFound) { op =>
      op.ask(x.message) pipeTo sender()
    }
  }

  def persistThrowable(state: State, e: Throwable): Unit = {
    persistException(state, e)
    throw e
  }

  override def postBehavior(state: State): Receive = {
    case StartCmd => preStart(state)
    case CompleteCmd(x: Info, y) => complete(state, x, y)
    case DeleteCmd(x: Info, y) => delete(state, x, y)
    case GetState => sender() ! StateRes(state)
    case x: Forward => forward(state, x)
    case x: ForwardWithReply => forwardWithReply(state, x)
    case x: Throwable => persistThrowable(state, x)
  }
}