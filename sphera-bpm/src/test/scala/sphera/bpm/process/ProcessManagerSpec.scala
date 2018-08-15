//defstorage sphera.bpm.process
//
//import akka.actor.ActorSystem
//import akka.testkit.TestKit
//import sphera.bpm.Exception.{ EntityAlreadyExistsException, EntityNotFoundException }
//import sphera.bpm.model.{ Modifier, ModifierType }
//import sphera.bpm.process.model.ProcessStatus
//import sphera.core.exception.SpheraException
//import sphera.core.persistence.Persistence.{ Pong, SnapshotInterval }
//import sphera.core.test.PersistenceSpec
//import org.scalatest.compatible.Assertion
//
//class ProcessManagerSpec extends TestKit(ActorSystem("ProcessManagerSpec"))
//  with PersistenceSpec
//  with NewProcessManager {
//
//  def assertProcessInfo(p2: ProcessInfo, y: CreateProcessInfo, modifier: Modifier): Assertion = {
//    p2.name shouldBe y.name
//    p2.description shouldBe y.description
//    p2.managerId shouldBe y.managerId
//    p2.modifyAttr.createdBy.userId shouldBe modifier.userId
//    p2.modifyAttr.createdBy.modifierType shouldBe modifier.modifierType
//    p2.startedBy shouldBe empty
//    p2.startedOn shouldBe empty
//  }
//
//  def assertProcessInfo(p2: ProcessInfo, y: UpdateProcessInfo, z: Modifier): Assertion = {
//    Option(p2.name) shouldBe y.name
//    Option(p2.description) shouldBe y.description
//    Option(p2.managerId) shouldBe y.managerId
//    p2.modifyAttr.createdBy.userId should not be empty
//    p2.modifyAttr.createdBy.modifierType shouldBe ModifierType.User
//    p2.modifyAttr.createdBy.userId should not be z.userId
//    p2.modifyAttr.updatedBy shouldBe Option(z)
//    p2.modifyAttr.updatedOn should not be empty
//    p2.startedBy shouldBe empty
//    p2.startedOn shouldBe empty
//  }
//
//  "A ProcessManager" when {
//    "createProcess" must {
//      "create processId" in {
//        val p2 = generateCreateProcessInfo()
//        val modifier = generateModifier
//        for {
//          manager <- newProcessManager()
//          x1 <- manager.createProcess(p2, modifier)
//          x2 <- manager.getProcessInfo(x1.id)
//          x3 <- manager.getProcessRef(x1.id)
//        } yield assertProcessInfo(x2, p2, modifier)
//      }
//    }
//    "startProcess" must {
//      "start processId" in {
//        val p2 = generateCreateProcessInfo()
//        val userId = generateUUID
//        val modifier = Modifier(userId = userId)
//        for {
//          manager <- newProcessManager()
//          x1 <- manager.createProcess(p2, modifier)
//          x2 <- manager.startProcess(x1.id, modifier)
//          x3 <- manager.getProcessInfo(x1.id)
//          x4 <- manager.getProcessRef(x1.id)
//          x5 <- ping(x4)
//        } yield {
//          assertProcessInfo(x1, p2, modifier)
//          x3.startedBy.get.userId shouldBe Option(userId)
//          x3.startedBy.get.modifierType shouldBe ModifierType.User
//          x3.startedOn should not be empty
//          x5 shouldBe Pong
//        }
//      }
//    }
//    "updateProcess" must {
//      "update processId" in {
//        val p2 = generateCreateProcessInfo()
//        val modifier = generateModifier
//        for {
//          manager <- newProcessManager()
//          x1 <- manager.createProcessAndGetId(p2, generateModifier)
//          x2 <- generateUpdateProcessInfo(x1)
//          x3 <- manager.updateProcess(x2, modifier)
//          x4 <- manager.getProcessInfo(x1)
//        } yield assertProcessInfo(x4, x2, modifier)
//      }
//      "not update processId when not exist" in {
//        recoverToSucceededIf[EntityNotFoundException] {
//          for {
//            manager <- newProcessManager()
//            x1 <- generateUpdateProcessInfo()
//            x2 <- manager.updateProcess(x1, generateModifier)
//          } yield x2
//        }
//      }
//    }
//    "deleteProcess" must {
//      "delete processId" in {
//        val a = generateCreateProcessInfo()
//        val b = generateCreateProcessInfo()
//        for {
//          manager <- newProcessManager()
//          x1 <- manager.createProcessAndGetId(a, generateModifier)
//          x2 <- manager.createProcessAndGetId(a, generateModifier)
//          x3 <- manager.deleteProcess(x1, Modifier(userId = generateUUID))
//          x4 <- manager.getProcessInfoAll
//        } yield {
//          x4 should have size 1
//          x4 should contain key x2
//        }
//      }
//      "not delete when processId not exist" in {
//        recoverToSucceededIf[EntityNotFoundException] {
//          for {
//            manager <- newProcessManager()
//            p2 <- manager.deleteProcess(generateString(), Modifier(userId = generateUUID))
//          } yield p2
//        }
//      }
//    }
//    "completeProcess" must {
//      "complete processId" in {
//        for {
//          manager <- newProcessManager()
//          x1 <- manager.createProcessAndGetId(generateCreateProcessInfo(), generateModifier)
//          x2 <- manager.startProcess(x1, generateModifier)
//          x3 <- manager.completeProcess(x1, generateModifier)
//          x4 <- manager.getProcessHistoryData(x1)
//        } yield x4.getExtendedInfo.info.status shouldBe RunnableStatus.Completed
//      }
//    }
//    "recover" must {
//      "create all processId instances" in {
//        val id = generateProcessManagerId
//        val p3 = SnapshotInterval / 2
//        for {
//          manager <- newProcessManager(id)
//          x1 <- manager.createProcessN(generateCreateProcessInfo(), p3, generateModifier)
//          x2 <- manager.getProcessInfoAll
//          _ <- kill(manager.processManagerActor)
//          manager <- newProcessManager(id)
//          x3 <- manager.getProcessInfoAll
//          x4 <- manager.getProcessRefs
//          x5 <- pingAll(x4)
//        } yield {
//          x1 should have size p3
//          x2 should have size p3
//          x3 should have size p3
//          x4 should have size p3
//          x5 should have size p3
//          x5 should contain only Pong
//        }
//      }
//    }
//    "snapshot" must {
//      "create all processId instances" in {
//        val id = generateProcessManagerId
//        val p3 = SnapshotInterval + SnapshotInterval / 2
//        for {
//          manager <- newProcessManager(id)
//          x1 <- manager.createProcessN(generateCreateProcessInfo(), p3, generateModifier)
//          x2 <- manager.getProcessInfoAll
//          _ <- kill(manager.processManagerActor)
//          manager <- newProcessManager(id)
//          x3 <- manager.getProcessInfoAll
//          x4 <- manager.getProcessRefs
//          x5 <- pingAll(x4)
//        } yield {
//          x1 should have size p3
//          x2 should have size p3
//          x3 should have size p3
//          x4 should have size p3
//          x5 should have size p3
//          x5 should contain only Pong
//        }
//      }
//    }
//  }
//}