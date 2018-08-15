package sphera.bpm.process

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.bpm.{ Bpm, OperationNotFoundException }
import sphera.bpm.model.Modifier
import sphera.bpm.process.Operation.{ ActiveOperations, BpmnId }
import sphera.bpm.runnable.{ RecoverInfo, RunnableManager }
import sphera.core.akkaext.actor._
import io.circe.Json

import scala.concurrent.{ ExecutionContext, Future }
import sphera.bpm.Implicits._
import sphera.bpm.process.actor.operation.TaskOperationActor
import sphera.bpm.task.TaskInfo

class ConcreteProcessManager(inject: => Bpm)(implicit val c: ExecutionContext, val t: Timeout) extends RunnableManager with ProcessManagerTypes {
  def bpm = inject
  def managerActor = bpm.processManager.processManagerActor
  def historyManager = bpm.processHistoryManager
}

trait ProcessManagerLike extends FutureSupport {
  val bpm: Bpm
  val concreteProcessManager = new ConcreteProcessManager(bpm)

  def getProcessInfo(processId: Process.Id) = concreteProcessManager.getRunnableInfo(processId)
  def getProcessInfoMap = concreteProcessManager.getRunnableInfoMap
  def getProcessInfoSet = concreteProcessManager.getRunnableInfoSet
  def getProcessExtendedInfo(processId: Process.Id) = concreteProcessManager.getRunnableExtendedInfo(processId)
  def getProcessExtendedInfoMap = concreteProcessManager.getRunnableExtendedInfoMap
  def getProcessState(processId: Process.Id) = concreteProcessManager.getRunnableState(processId)
  def getProcessStatus(processId: Process.Id) = concreteProcessManager.getRunnableStatus(processId)
  def getProcess(processId: Process.Id) = concreteProcessManager.getRunnable(processId)
  def getProcessOpt(processId: Process.Id) = concreteProcessManager.getRunnableOpt(processId)
  def getProcessActorRef(processId: Process.Id) = concreteProcessManager.getRunnableActorRef(processId)
  def getProcessActorRefOpt(processId: Process.Id) = concreteProcessManager.getRunnableActorRefOpt(processId)
  def getProcessActorRefSet = concreteProcessManager.getRunnableActorRefSet
  def createProcess(createInfo: CreateProcessInfo, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteProcessManager.createRunnable(createInfo, recoverInfo)
  def createProcessN(createInfo: CreateProcessInfo, n: Int, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteProcessManager.createRunnableN(createInfo, n, recoverInfo)
  def createProcessAndGetId(createInfo: CreateProcessInfo, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteProcessManager.createRunnableAndGetId(createInfo, recoverInfo)
  def createProcessAndGet(createInfo: CreateProcessInfo, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier): Future[Process] = concreteProcessManager.createRunnableAndGet(createInfo, recoverInfo)
  def startProcess(processId: Process.Id, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteProcessManager.startRunnable(processId, recoverInfo)
  def updateProcess(update: UpdateProcessInfo)(implicit modifier: Modifier) = concreteProcessManager.updateRunnable(update)
  def deleteProcess(processId: Process.Id)(implicit modifier: Modifier) = concreteProcessManager.deleteRunnable(processId)
  def completeProcess(processId: Process.Id, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier): Unit = concreteProcessManager.completeRunnable(processId, recoverInfo)
  def getProcessHistoryData(processId: Process.Id) = concreteProcessManager.getRunnableHistoryData(processId)
  def getProcessHistoryDataMap = concreteProcessManager.getRunnableHistoryDataMap
  def getProcessDef(processId: Process.Id, path: DataPath) = concreteProcessManager.getDef(Path(processId, path))
  def getProcessDefIntValue(processId: Process.Id, path: DataPath) = concreteProcessManager.getDefIntValue(Path(processId, path))
  def getProcessDefStringValue(processId: Process.Id, path: DataPath) = concreteProcessManager.getDefStringValue(Path(processId, path))
  def getProcessDefBooleanValue(processId: Process.Id, path: DataPath) = concreteProcessManager.getDefBooleanValue(Path(processId, path))
  def getProcessDefDecimalValue(processId: Process.Id, path: DataPath) = concreteProcessManager.getDefDecimalValue(Path(processId, path))
  def getProcessJson(processId: Process.Id, path: DataPath) = concreteProcessManager.getJson(Path(processId, path))
  def getProcessJson(processId: Process.Id) = concreteProcessManager.getJson(Path(processId, RootDataPath))
  def updateProcessJson(processId: Process.Id, path: DataPath, json: Json)(implicit modifier: Modifier) = concreteProcessManager.updateJson(Path(processId, path), json)
  def updateProcessJson(processId: Process.Id, json: Json)(implicit modifier: Modifier) = concreteProcessManager.updateJson(Path(processId, RootDataPath), json)
  def findProcessRoleUser(processId: Process.Id, role: String) = concreteProcessManager.findRoleUser(processId, role)
  def getActiveOperations(processId: Process.Id): Future[ActiveOperations] = getProcessState(processId).map(_.activeOperations)
  def isProcessStarted(processId: Process.Id) = concreteProcessManager.isRunnableStarted(processId)

  def getProcessIdByOperationId(operationId: Operation.Id): Future[Option[Process.Id]] = {
    getProcessInfoMap map { x =>
      val operationIds = x.mapValues(_.operationIds)
      val findPair = operationIds
        .flatMap(x => x._2.find(_ == operationId).map(opId => x._1 -> opId))
        .keys

      findPair.headOption
    }
  }

  def getProcessActorRefByOperationId(operationId: Operation.Id): Future[Option[ActorRef]] = {
    getProcessIdByOperationId(operationId).flatMap {
      case Some(x) => getProcessActorRefOpt(x)
      case None => Future.successful(None)
    }
  }

  def getProcessByOperationId(operationId: Operation.Id): Future[Option[Process]] = {
    getProcessIdByOperationId(operationId).flatMap {
      case Some(x) => getProcessOpt(x)
      case None => Future.successful(None)
    }
  }

  def completeOperation(taskInfo: TaskInfo): Future[TaskInfo] = {
    getProcessByOperationId(taskInfo.relaredOperationId.get) flatMap {
      case Some(x) => x.completeTaskOperation(taskInfo)
      case None => Future.failed(OperationNotFoundException(taskInfo.relatedOperationBpmnId.get))
    }
  }

  //def getProcessExceptions(processId: Process.Id) = concreteProcessManager.get
  //  def userTaskOperationComplete(processId: Process.Id, operationId: Operation.Id, definitions: Definitions): Future[Response] = Future {
  //    processManagerActor ! Forward(
  //      processId = processId,
  //      operationId = operationId,
  //      message = UserTaskOperationActor.Complete(definitions))
  //    Done
  //  }
  //
}