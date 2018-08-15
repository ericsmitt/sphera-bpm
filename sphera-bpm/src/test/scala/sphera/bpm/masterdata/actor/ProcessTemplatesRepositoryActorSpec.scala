package sphera.bpm.masterdata.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.masterdata.model.{ ProcessTemplate, UpdateProcessTemplate }
import sphera.core.test.PersistenceSpec
import org.scalatest.Assertion

import scala.concurrent.Future

class ProcessTemplatesRepositoryActorSpec extends TestKit(ActorSystem("ProcessTemplatesRepositoryActorSpec"))
  with PersistenceSpec
  with NewProcessTemplatesRepository {
  import ProcessTemplatesRepositoryActor._

  def assertProcess(x: ProcessTemplate, y: UpdateProcessTemplate): Assertion = {
    x.name shouldBe y.name
    x.description shouldBe y.description
    x.sourceCode shouldBe y.sourceCode
    x.dataStructureId shouldBe y.dataStructureId
    x.classification shouldBe y.classification
    x.modifyAttr.createdBy shouldBe y.userId
    x.modifyAttr.updatedBy shouldBe None
  }

  "A ProcessTemplatesRepositoryActor" when receive {
    "CreateCmd" must {
      "create new processId template in repository" in {
        for {
          d1 <- generateUpdateProcessTemplate()
          d2 <- generateUpdateProcessTemplate()
          a <- newProcessTemplatesRepositoryActor()
          x <- ask(a, CreateProcessTemplateCmd(d1))
          y <- ask(a, CreateProcessTemplateCmd(d2))
          z <- ask(a, GetProcessTemplateAll).mapTo[ProcessTemplateMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z should have size 2
          z should contain key d1.id
          z should contain key d2.id
          assertProcess(z(d1.id), d1)
          assertProcess(z(d2.id), d2)
        }
      }
    }
    "UpdateCmd" must {
      "update processId template in repository" in {
        for {
          d1 <- generateUpdateProcessTemplate()
          d2 <- generateUpdateProcessTemplate(d1.id)
          a <- newProcessTemplatesRepositoryActor()
          x <- ask(a, CreateProcessTemplateCmd(d1))
          y <- ask(a, UpdateProcessTemplateCmd(d2))
          z <- ask(a, GetProcessTemplateById(d1.id)).mapTo[ProcessTemplateOpt].map(_.x.get)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z.id shouldBe d1.id
          z.name shouldBe d2.name
          z.description shouldBe d2.description
          z.sourceCode shouldBe z.sourceCode
          z.dataStructureId shouldBe d2.dataStructureId
          z.classification shouldBe d2.classification
          z.modifyAttr.createdBy shouldBe d1.userId
          z.modifyAttr.updatedBy shouldBe Option(d2.userId)
        }
      }
      "not update when processId template not exist" in {
        for {
          d1 <- generateUpdateProcessTemplate()
          a <- newProcessTemplatesRepositoryActor()
          z <- ask(a, UpdateProcessTemplateCmd(d1))
        } yield z shouldBe NotFound
      }
    }
    "DeleteCmd" must {
      "delete processId template in repository" in {
        for {
          d1 <- generateUpdateProcessTemplate()
          d2 <- generateUpdateProcessTemplate()
          a <- newProcessTemplatesRepositoryActor()
          x <- ask(a, CreateProcessTemplateCmd(d1))
          y <- ask(a, CreateProcessTemplateCmd(d2))
          z <- ask(a, DeleteProcessTemplateCmd(d2.id))
          j <- ask(a, GetProcessTemplateAll).mapTo[ProcessTemplateMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z shouldBe Done
          j should have size 1
          j should contain key d1.id
        }
      }
      "not delete when processId template not exist" in {
        for {
          d1 <- generateUpdateProcessTemplate()
          a <- newProcessTemplatesRepositoryActor()
          z <- ask(a, DeleteProcessTemplateCmd(d1.id))
        } yield z shouldBe NotFound
      }
    }
  }
}