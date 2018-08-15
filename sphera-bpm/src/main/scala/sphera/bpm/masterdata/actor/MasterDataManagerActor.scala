package sphera.bpm.masterdata.actor

import akka.actor.SupervisorStrategy.{ Escalate, Restart, Resume }
import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props }
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.masterdata.MasterDataManager
import sphera.bpm.masterdata.actor.MasterDataManagerActor._
import sphera.core.akkaext.actor.ActorLifecycleHooks

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class MasterDataManagerActor(val id: MasterDataManager.Id, val bpm: Bpm)(implicit val c: ExecutionContext, val t: Timeout) extends Actor
  with ActorLogging
  with ActorLifecycleHooks {

  val characteristicRepository: ActorRef = context.actorOf(CharacteristicsRepositoryActor.props(id / CharacteristicRepositoryName), CharacteristicRepositoryName)
  val roleTypesRepository: ActorRef = context.actorOf(RoleTypesRepositoryActor.props(id / RoleTypesRepositoryName), RoleTypesRepositoryName)
  val fileTypesRepository: ActorRef = context.actorOf(FileTypesRepositoryActor.props(id / FileTypesRepositoryName), FileTypesRepositoryName)
  val dataStructuresRepository: ActorRef = context.actorOf(DataStructuresRepositoryActor.props(id / DataStructuresRepositoryName), DataStructuresRepositoryName)
  val formTemplatesRepository: ActorRef = context.actorOf(FormTemplatesRepositoryActor.props(id / FormTemplatesRepositoryName), FormTemplatesRepositoryName)
  val processTemplatesRepository: ActorRef = context.actorOf(ProcessTemplatesRepositoryActor.props(id / ProcessTemplatesRepositoryName, bpm), ProcessTemplatesRepositoryName)
  val projectTemplatesRepository: ActorRef = context.actorOf(ProjectTemplatesRepositoryActor.props(id / ProjectTemplatesRepositoryName), ProjectTemplatesRepositoryName)
  val calendarTypesRepository: ActorRef = context.actorOf(CalendarTypesRepositoryActor.props(id / CalendarTypesRepositoryName), CalendarTypesRepositoryName)

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

  def receive: Receive = {
    case x: CharacteristicsRepositoryActor.Command => characteristicRepository forward x
    case x: RoleTypesRepositoryActor.Command => roleTypesRepository forward x
    case x: FileTypesRepositoryActor.Command => fileTypesRepository forward x
    case x: DataStructuresRepositoryActor.Command => dataStructuresRepository forward x
    case x: FormTemplatesRepositoryActor.Command => formTemplatesRepository forward x
    case x: ProcessTemplatesRepositoryActor.Command => processTemplatesRepository forward x
    case x: ProjectTemplatesRepositoryActor.Command => projectTemplatesRepository forward x
    case x: CalendarTypesRepositoryActor.Command => calendarTypesRepository forward x

    case x: CharacteristicsRepositoryActor.Request => characteristicRepository forward x
    case x: RoleTypesRepositoryActor.Request => roleTypesRepository forward x
    case x: FileTypesRepositoryActor.Request => fileTypesRepository forward x
    case x: DataStructuresRepositoryActor.Request => dataStructuresRepository forward x
    case x: FormTemplatesRepositoryActor.Request => formTemplatesRepository forward x
    case x: ProcessTemplatesRepositoryActor.Request => processTemplatesRepository forward x
    case x: ProjectTemplatesRepositoryActor.Request => projectTemplatesRepository forward x
    case x: CalendarTypesRepositoryActor.Request => calendarTypesRepository forward x
  }
}

object MasterDataManagerActor {
  val CharacteristicRepositoryName = "characteristics"
  val RoleTypesRepositoryName = "roleTypes"
  val FileTypesRepositoryName = "fileTypes"
  val DataStructuresRepositoryName = "dataStructures"
  val FormTemplatesRepositoryName = "formTemplates"
  val ProcessTemplatesRepositoryName = "processTemplates"
  val ProjectTemplatesRepositoryName = "projectTemplates"
  val CalendarTypesRepositoryName = "calendarTypes"

  def props(id: MasterDataManager.Id, bpm: Bpm)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new MasterDataManagerActor(id, bpm))
  }
}