//package sphera.bpm.task
//
//import akka.actor.ActorSystem
//import akka.testkit.TestKit
//import sphera.bpm.Exception.EntityNotFoundException
//import sphera.bpm.model.{Modifier, ModifierType}
//import sphera.core.test.PersistenceSpec
//import org.scalatest.compatible.Assertion
//
//class TaskManagerSpec extends TestKit(ActorSystem("TaskManagerSpec"))
//  with PersistenceSpec
//  with NewTaskManager {
//  def assertTaskInfo(x: TaskInfo, y: CreateTaskInfo, z: Modifier): Assertion = {
//    x.name shouldBe y.name
//    x.description shouldBe y.description
//    x.taskType shouldBe y.taskType
//    x.status shouldBe TaskStatus.Created
//    x.initiator shouldBe y.initiator
//    x.ownerId shouldBe y.ownerId
//    x.assigneeId shouldBe y.assigneeId
//    x.reviewerId shouldBe y.reviewerId
//    x.watcherIds shouldBe y.watcherIds
//    x.processId shouldBe y.processId
//    x.operationId shouldBe y.operationId
//    x.operationBpmId shouldBe y.operationBpmnId
//    x.formTemplateId shouldBe y.formTemplateId
//    x.startedOn shouldBe empty
//    x.actualEnd shouldBe empty
//    x.plannedStart shouldBe y.plannedStart
//    x.plannedDuration shouldBe y.plannedDuration
//    x.modifyAttr.createdBy.userId shouldBe z.userId
//    x.modifyAttr.createdBy.modifierType shouldBe z.modifierType
//  }
//
//  def assertTaskInfo(x: TaskInfo, y: UpdateTaskInfo, z: Modifier): Assertion = {
//    x.id shouldBe y.id
//    Option(x.name) shouldBe y.name
//    Option(x.description) shouldBe y.description
//    Option(x.status) shouldBe y.status
//    Option(x.initiator) shouldBe y.initiator
//    Option(x.assigneeId) shouldBe y.assigneeId
//    Option(x.reviewerId) shouldBe y.reviewerId
//    Option(x.watcherIds) shouldBe y.watcherIds
//    x.startedOn shouldBe empty
//    x.actualEnd shouldBe empty
//    x.modifyAttr.createdBy.userId should not be empty
//    x.modifyAttr.createdBy.modifierType shouldBe ModifierType.User
//    x.modifyAttr.createdBy.userId should not be z.userId
//    x.modifyAttr.updatedBy shouldBe Option(z)
//    x.modifyAttr.updatedOn should not be empty
//  }
//
//  "A TaskManagerActor" when receive {
//    "createTask" must {
//      "create task" in {
//        val x = generateCreateTaskInfo()
//        val modifier = generateModifier
//        for {
//          manager <- newTaskManager()
//          x1 <- manager.createTask(x, modifier)
//          x2 <- manager.getTaskRef(x1.id)
//        } yield assertTaskInfo(x1, x, modifier)
//      }
//    }
//    "startTask" must {
//      "start task" in {
//        val x = generateCreateTaskInfo()
//        val modifier = generateModifier
//        for {
//          manager <- newTaskManager()
//          x1 <- manager.createTask(x, modifier)
//          x2 <- manager.startTask(x1.id, modifier)
//          x3 <- manager.getTaskInfo(x1.id)
//          x4 <- manager.getTaskRef(x1.id)
//          x5 <- ping(x4)
//        } yield {
//          assertTaskInfo(x1, x, modifier)
//          x3.startedOn should not be empty
//          x3.actualEnd shouldBe empty
//          x5 shouldBe Pong
//        }
//      }
//    }
//    "updateTask" must {
//      "update task" in {
//        val modifier = generateModifier
//        for {
//          manager <- newTaskManager()
//          x1 <- manager.createTaskAndGetId(generateCreateTaskInfo(), generateModifier)
//          x2 <- generateUpdateTaskInfo(x1)
//          x3 <- manager.updateTask(x2, modifier)
//          x4 <- manager.getTaskInfo(x1)
//        } yield assertTaskInfo(x4, x2, modifier)
//      }
//      "not update when task not exist" in {
//        recoverToSucceededIf[EntityNotFoundException] {
//          for {
//            manager <- newTaskManager()
//            x1 <- generateUpdateTaskInfo()
//            x2 <- manager.updateTask(x1, generateModifier)
//          } yield x2
//        }
//      }
//    }
//    "deleteTask" must {
//      "delete task" in {
//        val a = generateCreateTaskInfo()
//        val b = generateCreateTaskInfo()
//        for {
//          manager <- newTaskManager()
//          x1 <- manager.createTaskAndGetId(a, generateModifier)
//          x2 <- manager.createTaskAndGetId(a, generateModifier)
//          x3 <- manager.deleteTask(x1, Modifier(userId = generateUUID))
//          x4 <- manager.getTaskInfoAll
//        } yield {
//          x4 should have size 1
//          x4 should contain key x2
//        }
//      }
//      "not delete task when not exist" in {
//        recoverToSucceededIf[EntityNotFoundException] {
//          for {
//            manager <- newTaskManager()
//            x <- manager.deleteTask(generateString(), Modifier(userId = generateUUID))
//          } yield x
//        }
//      }
//    }
//    "deleteTasksByProcessId" must {
//      "delete tasks" in {
//        val processId = generateProcessId
//        val a = generateCreateTaskInfo(processId)
//        val b = generateCreateTaskInfo(processId)
//        for {
//          manager <- newTaskManager()
//          x1 <- manager.createTaskAndGetId(a, generateModifier)
//          x2 <- manager.createTaskAndGetId(a, generateModifier)
//          x3 <- manager.deleteTasksByProcessId(processId, generateModifier)
//          x4 <- manager.getTaskInfoAll
//        } yield x4 shouldBe empty
//      }
//    }
//    "completeTask" must {
//      "complete task" in {
//        for {
//          manager <- newTaskManager()
//          x1 <- manager.createTaskAndGetId(generateCreateTaskInfo(), generateModifier)
//          x2 <- manager.startTask(x1, generateModifier)
//          x3 <- manager.completeTask(x1, generateModifier)
//          x4 <- manager.getTaskHistoryData(x1)
//        } yield x4.extendedInfo.info.status shouldBe TaskStatus.Completed
//      }
//    }
//    "recover" must {
//      "create all tasks" in {
//        val id = generateTaskManagerId
//        val n = SnapshotInterval / 2
//        for {
//          manager <- newTaskManager(id)
//          x1 <- manager.createTaskN(generateCreateTaskInfo(), n, generateModifier)
//          x2 <- manager.getTaskInfoAll
//          _ <- kill(manager.taskManagerActor)
//          manager <- newTaskManager(id)
//          x3 <- manager.getTaskInfoAll
//          x4 <- manager.getTaskRefs
//          x5 <- pingAll(x4)
//        } yield {
//          x1 should have size n
//          x2 should have size n
//          x3 should have size n
//          x4 should have size n
//          x5 should have size n
//          x5 should contain only Pong
//
//          x3.keys should contain allElementsOf x1.map(_.id)
//        }
//      }
//    }
//    "snapshot" must {
//      "create all tasks" in {
//        val id = generateProcessManagerId
//        val n = SnapshotInterval + SnapshotInterval / 2
//        for {
//          manager <- newTaskManager(id)
//          x1 <- manager.createTaskN(generateCreateTaskInfo(), n, generateModifier)
//          x2 <- manager.getTaskInfoAll
//          _ <- kill(manager.taskManagerActor)
//          manager <- newTaskManager(id)
//          x3 <- manager.getTaskInfoAll
//          x4 <- manager.getTaskRefs
//          x5 <- pingAll(x4)
//        } yield {
//          x1 should have size n
//          x2 should have size n
//          x3 should have size n
//          x4 should have size n
//          x5 should have size n
//          x5 should contain only Pong
//
//          x3.keys should contain allElementsOf x1.map(_.id)
//        }
//      }
//    }
//  }
//}