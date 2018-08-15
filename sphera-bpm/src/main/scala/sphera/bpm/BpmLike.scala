package sphera.bpm

import akka.actor.ActorRef
import sphera.bpm.history.HistoryManager
import sphera.bpm.masterdata.MasterDataManager
import sphera.bpm.notification.NotificationManager
import sphera.bpm.process.ProcessManager
import sphera.bpm.project.ProjectManager
import sphera.bpm.storage.StorageManager
import sphera.bpm.task.TaskManager

trait BpmLike {
  def id: Bpm.Id
  def bpmActor: ActorRef
  def storageManager: StorageManager
  def masterDataManager: MasterDataManager
  def projectManager: ProjectManager
  def processManager: ProcessManager
  def taskManager: TaskManager
  def notificationManager: NotificationManager
  def projectHistoryManager: HistoryManager
  def processHistoryManager: HistoryManager
  def taskHistoryManager: HistoryManager
  def notificationManagerActor: ActorRef
  def masterDataManagerActor: ActorRef
}
