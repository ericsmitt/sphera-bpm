package sphera.bpm.process.actor.operation

import java.time.ZonedDateTime

import sphera.bpm._
import sphera.bpm.lang.{ Assignments, EvaluationContext, Evaluator }
import sphera.bpm.model.ImplicitModifier
import sphera.bpm.process.Operation._
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.project.Project
import sphera.bpm.task.Task
import sphera.core.akkaext.actor._
import sphera.core.akkaext.persistence._
import sphera.bpm.Implicits._

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

sealed trait OperationActorLike extends Evaluator with ImplicitModifier { _: CqrsActorBase =>
  val id: Operation.Id
  val bpmnId: Operation.BpmnId
  val name: Operation.Name
  val in: List[Operation.BpmnId]
  val out: List[Operation.BpmnId]
  val projectId: Project.Id
  val processId: Process.Id

  def bpm: Bpm
  def assignments: Assignments

  def activate(state: State, from: Option[Operation.BpmnId]): Unit
  def deactivate(): Unit = {
    parent ! Process.ActivateCmd(out, Option(bpmnId))
    log.info("deactivated")
  }

  def createContext(taskId: Option[Task.Id] = None, subProcessId: Option[Process.Id] = None) = {
    EvaluationContext(
      storage = bpm.storageManager,
      scopeMappings = Map(
        "$" -> Some(processId),
        "$process" -> Some(processId),
        "$project" -> Some(projectId),
        "$task" -> taskId,
        "$sub" -> subProcessId,
        "$subProcess" -> subProcessId,
        "$parent" -> None, // todo: Add parent scope
        "$storage" -> None) flatMap {
          case (k, None) => None
          case (k, Some(v)) => Some(k -> v)
        })
  }

  override val exceptionHandler: PartialFunction[Throwable, Unit] = {
    case e: Throwable =>
      val opException = OperationException(
        opId = id,
        opBpmnId = bpmnId,
        opName = name,
        cause = e)

      e.printStackTrace()

      throw opException
  }

  def isStartOperation: Boolean = this match {
    case x: StartOperationActor => true
    case x => false
  }

  def behavior(state: State): Receive = {
    case ActivateCmd(Some(from)) if !isStartOperation && !in.contains(from) =>
      throw InvalidActivationException(from)
    case ActivateCmd(x) =>
      log.info("activated")
      Try(activate(state, x)) match {
        case Success(_) =>
        case Failure(e) => exceptionHandler(e)
      }
  }
}

trait Connection

trait Connections {
  def ops: List[Operation.BpmnId]
  //def contains(op: Operation.BpmnId): Boolean
}

case class Inlet(bpmnId: Operation.BpmnId) extends Connection
case class Outlet(bpmnId: Operation.BpmnId) extends Connection

case class In(inlets: List[Inlet]) extends Connections {
  def ops = inlets.map(_.bpmnId)
}
case class Out(outlets: List[Outlet]) extends Connections {
  def ops = outlets.map(_.bpmnId)
}

trait OperationActor[State <: CqrsState] extends CqrsActor[State] with OperationActorLike
trait PersistentOperationActor[State <: CqrsState] extends CqrsPersistentActor[State] with OperationActorLike