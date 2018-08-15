package sphera.bpm.project

import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.history.HistoryManager
import sphera.bpm.model.Modifier
import sphera.bpm.runnable.{ RecoverInfo, RunnableManager }
import sphera.core.akkaext.actor.FutureSupport

import scala.concurrent.ExecutionContext

class ConcreteProjectManager(inject: => Bpm)(implicit val c: ExecutionContext, val t: Timeout) extends RunnableManager with ProjectManagerTypes {
  def bpm = inject
  def managerActor = bpm.projectManager.projectManagerActor
  def historyManager: HistoryManager = bpm.projectHistoryManager
}

trait ProjectManagerLike extends FutureSupport {
  val bpm: Bpm
  val concreteProjectManager = new ConcreteProjectManager(bpm)

  def getProjectInfo(projectId: Project.Id) = concreteProjectManager.getRunnableInfo(projectId)
  def getProjectInfoAll = concreteProjectManager.getRunnableInfoMap
  def getProjectInfoAllAsSet = concreteProjectManager.getRunnableInfoSet
  def getProjectExtendedInfo(projectId: Project.Id) = concreteProjectManager.getRunnableExtendedInfo(projectId)
  def getProjectStatus(projectId: Project.Id) = concreteProjectManager.getRunnableStatus(projectId)
  def getProject(projectId: Project.Id) = concreteProjectManager.getRunnable(projectId)
  def getProjectActorRef(projectId: Project.Id) = concreteProjectManager.getRunnableActorRef(projectId)
  def getProjectActorRefs = concreteProjectManager.getRunnableActorRefSet
  def createProject(createInfo: CreateProjectInfo, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteProjectManager.createRunnable(createInfo, recoverInfo)
  def createProjectN(createInfo: CreateProjectInfo, n: Int, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteProjectManager.createRunnableN(createInfo, n, recoverInfo)
  def createProjectAndGetId(createInfo: CreateProjectInfo, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteProjectManager.createRunnableAndGetId(createInfo, recoverInfo)
  def startProject(projectId: Project.Id, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier) = concreteProjectManager.startRunnable(projectId, recoverInfo)
  def updateProject(update: UpdateProjectInfo)(implicit modifier: Modifier) = concreteProjectManager.updateRunnable(update)
  def deleteProject(projectId: Project.Id)(implicit modifier: Modifier) = concreteProjectManager.deleteRunnable(projectId)
  def completeProject(projectId: Project.Id, recoverInfo: Option[RecoverInfo] = None)(implicit modifier: Modifier): Unit = concreteProjectManager.completeRunnable(projectId, recoverInfo)
  def getProjectHistoryData(projectId: Project.Id) = concreteProjectManager.getRunnableHistoryData(projectId)
  def getProjectHistoryDataAll = concreteProjectManager.getRunnableHistoryDataMap
}

//  def getActiveprojectes(projectId: project.Id): Future[Set[project.Id]] =
//    for {
//      a <- getprojectRef(projectId)
//      b <- ask(a, projectActor.GetState)
//        .mapTo[projectActor.State]
//        .map(_.node.activeprojectes)
//    } yield b
//  