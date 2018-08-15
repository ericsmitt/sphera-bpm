package sphera.bpm.actor

import akka.actor.SupervisorStrategy.{ Escalate, Restart, Resume }
import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props }
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.actor.BpmActor._
import sphera.bpm.history.HistoryManager
import sphera.bpm.history.actor.HistoryManagerActor
import sphera.bpm.masterdata.MasterDataManager
import sphera.bpm.masterdata.actor.MasterDataManagerActor
import sphera.bpm.notification.NotificationManager
import sphera.bpm.notification.actor.{ NotificationManagerActor, WebSocketNotificationActor }
import sphera.bpm.process.ProcessManager
import sphera.bpm.process.actor.ProcessManagerActor
import sphera.bpm.project.ProjectManager
import sphera.bpm.project.actor.ProjectManagerActor
import sphera.bpm.runnable.RunnableManager
import sphera.bpm.storage.StorageManager
import sphera.bpm.storage.actor.StorageManagerActor
import sphera.bpm.task.TaskManager
import sphera.bpm.task.actor.TaskManagerActor
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class BpmActor(val id: Bpm.Id, bpm: Bpm, config: Config)(implicit c: ExecutionContext, t: Timeout) extends Actor with ActorLogging {
  val storageManagerId = id / StorageManagerActorName
  val storageManagerActor = context.actorOf(
    StorageManagerActor.props(id = storageManagerId, bpm = bpm),
    name = StorageManagerActorName)
  val storageManager = StorageManager(storageActor = storageManagerActor, bpm = bpm)

  val masterDataManagerId = id / MasterDataManagerActorName
  val masterDataManagerActor: ActorRef = context.actorOf(
    props = MasterDataManagerActor.props(id = masterDataManagerId, bpm = bpm),
    name = MasterDataManagerActorName)
  val masterDataManager = MasterDataManager(masterDataManagerActor)

  val notificationManagerId = id / NotificationManagerActorName
  val notificationManagerActor: ActorRef = context.actorOf(
    props = NotificationManagerActor.props(
      id = notificationManagerId,
      config = config),
    name = NotificationManagerActorName)
  val notificationManager = NotificationManager(notificationManagerActor)

  val projectHistoryId = id / ProjectHistoryActorName
  val projectHistoryActor: ActorRef = context.actorOf(
    props = HistoryManagerActor.props(projectHistoryId),
    name = ProjectHistoryActorName)
  val projectHistoryManager = HistoryManager(projectHistoryActor)

  val projectManagerId = id / ProjectManagerActorName
  val projectManagerActor: ActorRef = context.actorOf(
    props = ProjectManagerActor.props(id = projectManagerId, bpm = bpm),
    name = ProjectManagerActorName)
  val projectManager = ProjectManager(projectManagerActor = projectManagerActor, bpm = bpm)

  val taskHistoryId = id / TaskHistoryActorName
  val taskHistoryActor: ActorRef = context.actorOf(
    props = HistoryManagerActor.props(taskHistoryId),
    name = TaskHistoryActorName)
  val taskHistoryManager = HistoryManager(taskHistoryActor)

  val taskManagerId = id / TaskManagerActorName
  val taskManagerActor: ActorRef = context.actorOf(
    props = TaskManagerActor.props(id = taskManagerId, bpm = bpm),
    name = TaskManagerActorName)
  val taskManager = TaskManager(taskManagerActor, bpm)

  val processHistoryId = id / ProcessHistoryActorName
  val processHistoryActor: ActorRef = context.actorOf(
    props = HistoryManagerActor.props(processHistoryId),
    name = ProcessHistoryActorName)
  val processHistoryManager = HistoryManager(processHistoryActor)

  val processManagerId = id / ProcessManagerActorName
  val processManagerActor: ActorRef = context.actorOf(
    props = ProcessManagerActor.props(id = processManagerId, bpm = bpm),
    name = ProcessManagerActorName)
  val processManager = ProcessManager(processManagerActor = processManagerActor, bpm = bpm)

  override val supervisorStrategy: OneForOneStrategy = {
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case e: ArithmeticException =>
        log.error(e, e.getMessage)
        Resume
      case e: NullPointerException =>
        log.error(e, e.getMessage)
        Restart
      case e: IllegalArgumentException =>
        log.debug(e.getMessage)
        Resume
      case e: Exception =>
        log.error(e, e.getMessage)
        Restart
      case e =>
        super.supervisorStrategy.decider.applyOrElse(e, (_: Any) => Escalate)
    }
  }

  def managers(): Unit = {
    sender() ! Managers(
      storageManager = storageManager,
      masterDataManager = masterDataManager,
      projectManager = projectManager,
      processManager = processManager,
      taskManager = taskManager,
      notificationManager = notificationManager,
      projectHistoryManager = projectHistoryManager,
      processHistoryManager = processHistoryManager,
      taskHistoryManager = taskHistoryManager)
  }

  def receive: Receive = {
    case x: RunnableManager.Command => processManagerActor forward x
    case x: RunnableManager.Request => processManagerActor forward x
    case x: WebSocketNotificationActor.Command => notificationManagerActor forward x
    case GetManagers => managers()
    case x => masterDataManagerActor forward x
  }
}

object BpmActor {
  val MasterDataManagerActorName = "masterData"
  val ProjectManagerActorName = "project"
  val ProjectHistoryActorName = "projectHistory"
  val ProcessManagerActorName = "process"
  val ProcessHistoryActorName = "processHistory"
  val TaskManagerActorName = "task"
  val TaskHistoryActorName = "taskHistory"
  val NotificationManagerActorName = "notification"
  val StorageManagerActorName = "storage"

  case object GetManagers

  case class Managers(
    storageManager: StorageManager,
    masterDataManager: MasterDataManager,
    projectManager: ProjectManager,
    processManager: ProcessManager,
    taskManager: TaskManager,
    notificationManager: NotificationManager,
    projectHistoryManager: HistoryManager,
    processHistoryManager: HistoryManager,
    taskHistoryManager: HistoryManager)

  def props(id: Bpm.Id, bpm: Bpm, config: Config = ConfigFactory.load())(implicit c: ExecutionContext, t: Timeout) =
    Props(new BpmActor(id, bpm = bpm, config = config))
}
