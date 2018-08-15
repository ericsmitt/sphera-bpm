package sphera.bpm.process

import akka.testkit.TestKit
import sphera.bpm.Implicits._
import sphera.bpm.json._
import sphera.bpm.masterdata.actor.NewMasterDataManager
import sphera.bpm.masterdata.model.ProcessTemplate
import sphera.bpm.process.actor.ProcessState
import sphera.core.test.PersistenceSpec

trait NewProcessManager extends NewMasterDataManager { _: PersistenceSpec with TestKit =>
  // def generateProcessManagerId = ActorId(s"processManager-$generateInt")
  // def generateProcessId = ActorId(s"processId-$generateInt")
  //def generateOperationId = ActorId(s"operation-$generateInt")

  //  def newProcessManager(
  //    id: ProcessManager.Id,
  //    masterData: MasterDataManager,
  //    taskManager: TaskManager): Future[ProcessManager] = {
  //    for (p2 <- newHistoryManager()) yield {
  //      ProcessManager(
  //        processManagerActor =
  //          system.actorOf(
  //            props = ProcessManagerActor.props(
  //              id = id,
  //              masterDataManager = masterData,
  //              taskManager = taskManager,
  //              historyManager = p2,
  //              notificationManager = newNotificationManager),
  //            name = id),
  //        masterDataManager = masterData,
  //        processHistoryManager = p2)
  //    }
  //  }

  def generateCreateProcessInfo(state: Option[ProcessState] = None, templateId: ProcessTemplate.Id = ProcessTemplates.StartEnd.id): CreateProcessInfo = {
    val data = """
        |{
        |  "value" : {
        |    "company" : {
        |      "name" : "updated",
        |      "value" : "updated"
        |    },
        |    "secretary" : {
        |      "description" : "updated",
        |      "value" : {
        |        "changeable" : {
        |          "value" : false
        |        }
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    CreateProcessInfo(
      name = s"subProcessId-$generateInt",
      description = Some(s"desc-$generateInt"),
      data = parse(data),
      projectId = generateActorId,
      managerId = generateUUID,
      templateId = templateId,
      relatedOperationId = None,
      relatedOperationBpmnId = None,
      initState = state)
  }

  //  def generateUpdateProcessInfo(id: Process.Id = generateProcessId): Future[UpdateProcessInfo] = Future {
  //    UpdateProcessInfo(
  //      id = id,
  //      name = Option(s"name-$generateInt"),
  //      description = Option(Option(s"desc-$generateInt")),
  //      status = Option(RunnableStatus.Created),
  //      managerId = Option(generateUUID),
  //      startedBy = None,
  //      startedOn = None)
  //  }

  //  def newProcessManager(
  //    id: ProcessManager.Id = generateProcessManagerId,
  //    taskManager: TaskManager = newTaskManager): Future[ProcessManager] = {
  //    for {
  //      p2 <- newFilledMasterDataManager
  //      y <- newProcessManager(
  //        id = id,
  //        masterData = p2.masterDataManager,
  //        taskManager = taskManager)
  //    } yield y
  //  }
  //
  //  def newProcessManagerA(
  //    id: ProcessManager.Id = generateProcessManagerId,
  //    taskManager: TaskManager = newTaskManager,
  //    processTemplateId: ProcessTemplate.Id = startEndTemplateId): Future[(ProcessManager, ProcessTemplate)] = {
  //    for {
  //      FilledMasterDataManager(masterDataManager, processTemplates, _, _) <- newFilledMasterDataManager
  //      p2 <- newProcessManager(
  //        id = id,
  //        masterData = masterDataManager,
  //        taskManager = taskManager)
  //    } yield (p2, processTemplates.find(_.id == processTemplateId).get)
  //  }
  //
  //  def newProcessRuntimeB(
  //    id: ProcessManager.Id = generateProcessManagerId,
  //    taskManager: TaskManager = newTaskManager): Future[(ActorRef, ProcessManager.Id)] = {
  //    for {
  //      p2 <- newFilledMasterDataManager
  //      y <- newProcessManager(
  //        id = id,
  //        masterData = p2.masterDataManager,
  //        taskManager = taskManager)
  //    } yield (y.processManagerActor, id)
  //  }
  //
  //  private def newTaskManager = TaskManager(newTestProbeRef, HistoryManager(newTestProbeRef))
  //  private def newNotificationManager = NotificationManager(newTestProbeRef)
}