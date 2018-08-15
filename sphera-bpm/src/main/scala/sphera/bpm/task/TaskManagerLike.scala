package sphera.bpm.task

import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.model.Modifier
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.runnable.{ RecoverInfo, RunnableManager }
import sphera.core.akkaext.actor._
import io.circe.Json

import scala.concurrent.{ ExecutionContext, Future }

class ConcreteTaskManager(inject: => Bpm)(implicit val c: ExecutionContext, val t: Timeout) extends RunnableManager with TaskManagerTypes {
  def bpm = inject
  def managerActor = bpm.taskManager.taskManagerActor
  def historyManager = bpm.taskHistoryManager
}

trait TaskManagerLike extends FutureSupport {
  val bpm: Bpm
  val concreteTaskManager = new ConcreteTaskManager(bpm)

  def getTasksIds = concreteTaskManager.getRunnablesIdSet
  def getTaskInfo(taskId: Task.Id) = concreteTaskManager.getRunnableInfo(taskId)
  def getTaskInfoAll = concreteTaskManager.getRunnableInfoMap
  def getTaskInfoAllAsSet = concreteTaskManager.getRunnableInfoSet
  def getTaskExtendedInfo(taskId: Task.Id) = concreteTaskManager.getRunnableExtendedInfo(taskId)
  def getTaskStatus(taskId: Task.Id) = concreteTaskManager.getRunnableStatus(taskId)
  def getTask(taskId: Task.Id) = concreteTaskManager.getRunnable(taskId)
  def getUserTask(taskId: Task.Id) = concreteTaskManager.getRunnable(taskId).mapTo[UserTask]
  def getTaskActorRef(taskId: Task.Id) = concreteTaskManager.getRunnableActorRef(taskId)
  def getTaskActorRefs = concreteTaskManager.getRunnableActorRefSet
  def createTask(createInfo: CreateTaskInfo, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteTaskManager.createRunnable(createInfo, recoverInfo)
  def createTaskN(createInfo: CreateTaskInfo, n: Int, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteTaskManager.createRunnableN(createInfo, n, recoverInfo)
  def createTaskAndGetId(createInfo: CreateTaskInfo, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteTaskManager.createRunnableAndGetId(createInfo, recoverInfo)
  def startTask(taskId: Task.Id, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier): Future[TaskInfo] = concreteTaskManager.startRunnable(taskId, recoverInfo)
  def startTaskAndGetId(taskId: Task.Id, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteTaskManager.startRunnableAndGetId(taskId, recoverInfo)
  def updateTask(update: UpdateTaskInfo)(implicit modifier: Modifier) = concreteTaskManager.updateRunnable(update)
  def deleteTask(taskId: Task.Id)(implicit modifier: Modifier) = concreteTaskManager.deleteRunnable(taskId)
  def completeTask(taskId: Task.Id, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier): Unit = concreteTaskManager.completeRunnable(taskId, recoverInfo)
  def getTaskHistoryData(taskId: Task.Id) = concreteTaskManager.getRunnableHistoryData(taskId)
  def getTaskHistoryDataAll = concreteTaskManager.getRunnableHistoryDataMap
  def getTaskDef(taskId: Task.Id, path: DataPath) = concreteTaskManager.getDef(Path(taskId, path))
  def getTaskJson(taskId: Task.Id, path: DataPath) = concreteTaskManager.getJson(Path(taskId, path))
  def getTaskJson(taskId: Task.Id) = concreteTaskManager.getJson(Path(taskId, RootDataPath))
  def updateTaskJson(taskId: Task.Id, path: DataPath, json: Json)(implicit modifier: Modifier) = concreteTaskManager.updateJson(Path(taskId, path), json)
  def updateTaskJson(taskId: Task.Id, json: Json)(implicit modifier: Modifier) = concreteTaskManager.updateJson(Path(taskId, RootDataPath), json)
  def deleteTasksByProcessId(processId: Process.Id) = ???
  def submitTaskForm(taskId: Task.Id, data: Json)(implicit modifier: Modifier): Future[Task.Id] = getUserTask(taskId).flatMap(_.submitForm(data))
  def completeSubProcess(taskId: Task.Id) = getTaskActorRef(taskId).flatMap(_.ask(Task.CompleteSubProcessCmd))

  def getTaskIdBySubProcessId(subProcessId: Process.Id): Future[Option[Task.Id]] =
    getTaskInfoAll
      .map(_.find(_._2.subProcessId contains subProcessId))
      .map(_.map(_._2.id))

  def getTasksByProcessId(processId: Process.Id): Future[Set[Task.Id]] =
    getTaskInfoAll
      .map(_.filter(_._2.processId == processId))
      .map(_.map(_._2.id).toSet)

  def forwardToTask(taskId: Task.Id, message: Any) = concreteTaskManager.forward(taskId, message)
}