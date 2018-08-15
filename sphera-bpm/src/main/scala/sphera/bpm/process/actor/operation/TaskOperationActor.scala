package sphera.bpm.process.actor.operation

import java.time.ZonedDateTime
import java.util.UUID

import sphera.bpm.InvalidCompleteTaskException
import sphera.bpm.json._
import sphera.bpm.lang.Assignments
import sphera.bpm.masterdata.model.DataStructure
import sphera.bpm.process.Operation
import sphera.bpm.process.Operation.Done
import sphera.bpm.process.actor.operation.LoopCharacteristics._
import sphera.bpm.process.actor.operation.TaskOperationActor._
import sphera.bpm.runnable.{RecoverInfo, RunnableType}
import sphera.bpm.storage.definition.{DefStorage, DefStorageBuilder}
import sphera.bpm.task.TaskInfo
import sphera.core.akkaext.actor._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait TaskOperationActor extends PersistentOperationActor[TaskOperationState] with DefStorageBuilder {
  val loopCharacteristics: LoopCharacteristics
  val dataStructureId: Option[DataStructure.Id]
  val plannedStart: Option[ZonedDateTime]
  val plannedDuration: Option[FiniteDuration]
  val preAssignments: Assignments
  val postAssignments: Assignments
  val initState: TaskOperationState = TaskOperationState.empty

  def createTask(defStorage: DefStorage, recoverInfo: Option[RecoverInfo]): Future[TaskInfo]

  def assignments = preAssignments ++ postAssignments

  def activate(state: TaskOperationState, from: Option[Operation.BpmnId]): Unit = {
    from match {
      case Some(x) => activationStarted(state, generateUUID, x, None)
      case None =>
        // recover from history
        state.activationStartedHistory.foreach(x => self ! RecoverActivationStartedCmd(x._1, x._2))
        state.activationCreatedTaskHistory.foreach(x => self ! RecoverActivationCreatedTaskCmd(x._1, x._2))
        state.activationAssignmentsEvaluatedHistory.foreach(x => self ! RecoverActivationAssignmentsEvaluatedCmd(x._1, x._2))
        state.activationStartTaskHistory.foreach(x => self ! RecoverActivationTaskStartCmd(x._1, x._2))
        state.completeStartedHistory.foreach(x => self ! RecoverCompleteStartedCmd(x._1, x._2))
        state.completeAssignmentsEvaluatedHistory.foreach(x => self ! RecoverCompleteAssignmentsEvaluatedCmd(x._1, x._2))
    }
  }

  def activationStarted(state: TaskOperationState, activationId: UUID, from: Operation.BpmnId, recoverInfo: Option[RecoverInfo]): Unit = {
    persist(state, ActivationStartedEvt(activationId, from)) { (state, event) =>
      afterNoReply(
        build(dataStructureId).flatMap(createTask(_, recoverInfo))) { (taskInfo, state) =>
        activationCreatedTask(state, activationId, taskInfo)
      }
    }
  }

  def activationCreatedTask(state: TaskOperationState, activationId: UUID, taskInfo: TaskInfo): Unit = {
    persist(state, ActivationCreatedTaskEvt(activationId, taskInfo)) { (state, event) =>
      afterNoReply(evalAssignments(preAssignments, createContext(Some(taskInfo.id), taskInfo.subProcessId))) { (response, state) =>
        activationAssignmentsEvaluated(state, activationId, taskInfo, None)
      }
    }
  }

  def activationAssignmentsEvaluated(state: TaskOperationState, activationId: UUID, taskInfo: TaskInfo, recoverInfo: Option[RecoverInfo]): Unit = {
    persist(state, ActivationAssignmentsEvaluatedEvt(activationId, taskInfo)) { (state, event) =>
      loopCharacteristics match {
        case StandardLoop =>
        case MultiInstanceSequential if state.activationStartTaskHistory.isEmpty =>
          activationStartTask(state, activationId, taskInfo, recoverInfo)
        case MultiInstanceSequential =>
          // if started just wait previous task
        case MultiInstanceParallel =>
          activationStartTask(state, activationId, taskInfo, recoverInfo)
      }
    }
  }

  def activationStartTask(state: TaskOperationState, activationId: UUID, taskInfo: TaskInfo, recoverInfo: Option[RecoverInfo]): Unit = {
    // must first persist event than start task
    persist(state, ActivationStartTaskEvt(activationId, taskInfo)) { (state, event) =>
      afterNoReply(bpm.startTask(taskInfo.id, recoverInfo)) { (taskInfo, state) =>
        activationStartedTask(state, activationId, taskInfo)
      }
    }
  }

  def activationStartedTask(state: TaskOperationState, activationId: UUID, taskInfo: TaskInfo): Unit = {
    persistOnly(state, ActivationTaskStartedEvt(activationId, taskInfo))
  }

  def completeStarted(state: TaskOperationState, activationIdOpt: Option[UUID], taskInfo: TaskInfo, recoverInfo: Option[RecoverInfo]): Unit = {
    val activationId = activationIdOpt.getOrElse(getActivationId(state, taskInfo))
    persist(state, CompleteStartedEvt(activationId, taskInfo)) { (state, event) =>
      after(evalAssignments(postAssignments, createContext(Some(taskInfo.id), taskInfo.subProcessId))) { (response, state) =>
        recoverInfo match  {
          case Some(x) => bpm.completeTask(taskInfo.id, recoverInfo)
          case None =>
            // must complete subProcess ifAny only after assignments evaluation
            taskInfo.subProcessId.foreach(bpm.completeProcess(_))
            sender() ! Done
        }

        completeAssignmentsEvaluated(state, activationId, taskInfo)

      }
    }
  }

  def completeAssignmentsEvaluated(state: TaskOperationState, activationId: UUID, taskInfo: TaskInfo): Unit = {
    persist(state, CompleteAssignmentsEvaluatedEvt(activationId, taskInfo)) { (state, event) =>
      deactivate()

      loopCharacteristics match {
        case StandardLoop =>
        case MultiInstanceSequential if state.activationAssignmentsEvaluatedHistory.nonEmpty =>
          val (activationId, taskInfo) = state.activationAssignmentsEvaluatedHistory.head
          activationStartTask(state, activationId, taskInfo, None)
          completeDeactivated(state, activationId, taskInfo)
        case MultiInstanceSequential =>
          completeDeactivated(state, activationId, taskInfo)
        case MultiInstanceParallel =>
          completeDeactivated(state, activationId, taskInfo)
      }
    }
  }

  def completeDeactivated(state: TaskOperationState,  activationId: UUID, taskInfo: TaskInfo): Unit = {
    persistOnly(state, CompleteDeactivatedEvt(activationId, taskInfo))
  }

  def invalidCompleteTask(state: TaskOperationState, taskInfo: TaskInfo): UUID = {
    log.error("ActivationStartTaskHistory: " + state.activationStartTaskHistory.asJsonStr)
    log.error("ActivationStartedTaskHistory: " + state.activationStartedTaskHistory.asJsonStr)
    throw InvalidCompleteTaskException(taskInfo)
  }

  def isInvalidComplete(state: TaskOperationState, taskInfo: TaskInfo): Boolean = {
    !(state.activationStartTaskHistory.exists(_._2.id == taskInfo.id) ||
      state.activationStartedTaskHistory.exists(_._2.id == taskInfo.id))
  }

  def getActivationId(state: TaskOperationState, taskInfo: TaskInfo): UUID = {
    val x1 = state.activationStartTaskHistory.find(_._2.id == taskInfo.id)
    val x2 = state.activationStartedTaskHistory.find(_._2.id == taskInfo.id)
    (x1, x2) match {
      case (Some((activationId, _)), _) => activationId
      case (_, Some((activationId, _))) => activationId
      case _ => invalidCompleteTask(state, taskInfo)
    }
  }

  override def postBehavior(state: TaskOperationState): Receive = {
    case CompleteTaskCmd(taskInfo) if isInvalidComplete(state, taskInfo) => invalidCompleteTask(state, taskInfo)
    case CompleteTaskCmd(taskInfo) => completeStarted(state, None, taskInfo, None)
    case RecoverActivationStartedCmd(x, y) => activationStarted(state, x, y, Some(RecoverInfo(RunnableType.Task)))
    case RecoverActivationCreatedTaskCmd(x, y) => activationCreatedTask(state, x, y)
    case RecoverActivationAssignmentsEvaluatedCmd(x, y) => activationAssignmentsEvaluated(state, x, y, Some(RecoverInfo(RunnableType.Task)))
    case RecoverActivationTaskStartCmd(x, y) => activationStartTask(state, x, y, Some(RecoverInfo(RunnableType.Task)))
    case RecoverActivationTaskStartedCmd(x, y) => activationStartedTask(state, x, y)
    case RecoverCompleteStartedCmd(x, y) => completeStarted(state, Some(x), y, Some(RecoverInfo(RunnableType.Task)))
    case RecoverCompleteAssignmentsEvaluatedCmd(x, y) => completeAssignmentsEvaluated(state, x, y)
    case RecoverCompleteDeactivatedCmd(x, y) => completeDeactivated(state, x, y)
  }
}

case class TaskOperationState(
  activationStartedHistory: Map[UUID, Operation.BpmnId],
  activationCreatedTaskHistory: Map[UUID, TaskInfo],
  activationAssignmentsEvaluatedHistory: Map[UUID, TaskInfo],
  activationStartTaskHistory: Map[UUID, TaskInfo],
  activationStartedTaskHistory: Map[UUID, TaskInfo],
  completeStartedHistory: Map[UUID, TaskInfo],
  completeAssignmentsEvaluatedHistory: Map[UUID, TaskInfo]) extends CqrsState {

  def update: Update = {
    case ActivationStartedEvt(x, y) =>
      copy(activationStartedHistory = activationStartedHistory + (x -> y))
    case ActivationCreatedTaskEvt(x, y) => copy(
      activationStartedHistory = activationStartedHistory - x,
      activationCreatedTaskHistory = activationCreatedTaskHistory + (x -> y)
    )
    case ActivationAssignmentsEvaluatedEvt(x, y) => copy(
      activationCreatedTaskHistory = activationCreatedTaskHistory.filterNot(_._1 == x),
      activationAssignmentsEvaluatedHistory = activationAssignmentsEvaluatedHistory + (x -> y)
    )
    case ActivationStartTaskEvt(x, y) => copy(
      activationAssignmentsEvaluatedHistory = activationAssignmentsEvaluatedHistory.filterNot(_._1 == x),
      activationStartTaskHistory = activationStartTaskHistory + (x -> y)
    )
    case ActivationTaskStartedEvt(x, y) => copy(
      activationStartTaskHistory = activationStartTaskHistory.filterNot(_._1 == x),
      activationStartedTaskHistory = activationStartedTaskHistory + (x -> y)
    )
    case CompleteStartedEvt(x, y) => copy(
      activationStartTaskHistory = activationStartTaskHistory.filterNot(_._1 == x),
      activationStartedTaskHistory = activationStartedTaskHistory.filterNot(_._1 == x),
      completeStartedHistory = completeStartedHistory + (x -> y)
    )
    case CompleteAssignmentsEvaluatedEvt(x, y) => copy(
      completeStartedHistory = completeStartedHistory.filterNot(_._1 == x),
      completeAssignmentsEvaluatedHistory = completeAssignmentsEvaluatedHistory + (x -> y)
    )
    case CompleteDeactivatedEvt(x, y) => copy(
      completeAssignmentsEvaluatedHistory = completeAssignmentsEvaluatedHistory.filterNot(_._1 == x)
    )
  }
}

object TaskOperationState {
  def empty: TaskOperationState = TaskOperationState(
    activationStartedHistory = Map.empty,
    activationCreatedTaskHistory = Map.empty,
    activationAssignmentsEvaluatedHistory = Map.empty,
    activationStartTaskHistory = Map.empty,
    activationStartedTaskHistory = Map.empty,
    completeStartedHistory = Map.empty,
    completeAssignmentsEvaluatedHistory = Map.empty,
  )
}

trait LoopCharacteristics

object LoopCharacteristics {
  case object StandardLoop extends LoopCharacteristics
  case object MultiInstanceParallel extends LoopCharacteristics
  case object MultiInstanceSequential extends LoopCharacteristics
}

object TaskOperationActor {
  trait Command extends Operation.Command
  trait Request extends Operation.Request
  trait Response extends Operation.Response
  trait Event extends Operation.Event

  case class RecoverActivationStartedCmd(activationId: UUID, from: Operation.BpmnId) extends Command
  case class RecoverActivationCreatedTaskCmd(activationId: UUID, taskInfo: TaskInfo) extends Command
  case class RecoverActivationAssignmentsEvaluatedCmd(activationId: UUID, taskInfo: TaskInfo) extends Command
  case class RecoverActivationTaskStartCmd(activationId: UUID, taskInfo: TaskInfo) extends Command
  case class RecoverActivationTaskStartedCmd(activationId: UUID, taskInfo: TaskInfo) extends Command

  case class ActivationStartedEvt(activationId: UUID, from: Operation.BpmnId) extends Event
  case class ActivationCreatedTaskEvt(activationId: UUID, taskInfo: TaskInfo) extends Event
  case class ActivationAssignmentsEvaluatedEvt(activationId: UUID, taskInfo: TaskInfo) extends Event
  case class ActivationStartTaskEvt(activationId: UUID, taskInfo: TaskInfo) extends Event
  case class ActivationTaskStartedEvt(activationId: UUID, taskInfo: TaskInfo) extends Event

  case class RecoverCompleteStartedCmd(activationId: UUID, taskInfo: TaskInfo) extends Command
  case class RecoverCompleteAssignmentsEvaluatedCmd(activationId: UUID, taskInfo: TaskInfo) extends Command
  case class RecoverCompleteDeactivatedCmd(activationId: UUID, taskInfo: TaskInfo) extends Command

  case class CompleteStartedEvt(activationId: UUID, taskInfo: TaskInfo) extends Event
  case class CompleteAssignmentsEvaluatedEvt(activationId: UUID, taskInfo: TaskInfo) extends Event
  case class CompleteDeactivatedEvt(activationId: UUID, taskInfo: TaskInfo) extends Event

  case class CompleteTaskCmd(taskInfo: TaskInfo) extends Command
}