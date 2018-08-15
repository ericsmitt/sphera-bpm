package sphera.bpm

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.util.Timeout
import sphera.bpm.actor.BpmActor
import sphera.bpm.actor.BpmActor.{ GetManagers, Managers }
import sphera.bpm.history.HistoryManager
import sphera.bpm.masterdata.{ MasterDataManager, MasterDataManagerLike }
import sphera.bpm.notification.{ NotificationManager, NotificationManagerLike }
import sphera.bpm.process.{ ProcessManager, ProcessManagerLike }
import sphera.bpm.project.{ ProjectManager, ProjectManagerLike }
import sphera.bpm.storage.{ StorageManager, StorageManagerLike }
import sphera.bpm.task.{ TaskManager, TaskManagerLike }
import sphera.core.akkaext.actor.{ ActorId, CqrsEvent }
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Future API для работы с BPM движком
 *
 * Api является композицией из:
 *  - [[MasterDataManagerLike]]
 *  - [[NotificationManagerLike]]
 *  - [[ProjectManagerLike]]
 *  - [[ProcessManagerLike]]
 *  - [[TaskManagerLike]]
 *  - [[StorageManagerLike]]
 */
class Bpm(val id: Bpm.Id)(implicit val c: ExecutionContext, val t: Timeout)
  extends MasterDataManagerLike
  with NotificationManagerLike
  with ProjectManagerLike
  with ProcessManagerLike
  with TaskManagerLike
  with StorageManagerLike {

  val bpm = this
  var bpmActor: ActorRef = _
  var storageManager: StorageManager = _
  var masterDataManager: MasterDataManager = _
  var projectManager: ProjectManager = _
  var processManager: ProcessManager = _
  var taskManager: TaskManager = _
  var notificationManager: NotificationManager = _
  var projectHistoryManager: HistoryManager = _
  var processHistoryManager: HistoryManager = _
  var taskHistoryManager: HistoryManager = _

  def notificationManagerActor: ActorRef = notificationManager.notificationManagerActor
  def masterDataManagerActor: ActorRef = masterDataManager.masterDataManagerActor
  def storageManagerActor: ActorRef = storageManager.storageManagerActor
}

object Bpm {
  trait Event extends CqrsEvent

  case class CreatedEvt(bpmId: Bpm.Id) extends Event

  type Id = ActorId

  val bpmName = "bpm"

  /**
   * Create new Bpm instance
   */
  def apply(id: Bpm.Id, config: Config = ConfigFactory.load())(implicit system: ActorSystem, c: ExecutionContext, t: Timeout): Future[Bpm] = {
    val bpm = new Bpm(id)
    val bpmActor = system.actorOf(props = BpmActor.props(id, bpm, config), name = id.name)

    def dependencies: Future[Managers] = ask(bpmActor, GetManagers).mapTo[Managers].map { managers =>
      bpm.bpmActor = bpmActor
      bpm.storageManager = managers.storageManager
      bpm.masterDataManager = managers.masterDataManager
      bpm.projectManager = managers.projectManager
      bpm.processManager = managers.processManager
      bpm.taskManager = managers.taskManager
      bpm.notificationManager = managers.notificationManager
      bpm.projectHistoryManager = managers.projectHistoryManager
      bpm.processHistoryManager = managers.processHistoryManager
      bpm.taskHistoryManager = managers.taskHistoryManager
      managers
    }

    for {
      managers <- dependencies
    } yield {
      system.eventStream.publish(CreatedEvt(id))
      bpm
    }
  }
}
