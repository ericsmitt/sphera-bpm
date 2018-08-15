package sphera.bpm.masterdata.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.masterdata.model.{ FormTemplate, UpdateFormTemplate }
import sphera.core.test.PersistenceSpec
import org.scalatest.Assertion

class FormTemplatesRepositoryActorSpec extends TestKit(ActorSystem("FormTemplatesRepositoryActorSpec"))
  with PersistenceSpec
  with NewFormTemplatesRepository {
  import FormTemplatesRepositoryActor._

  def assertFormTemplate(x: FormTemplate, y: UpdateFormTemplate): Assertion = {
    x.name shouldBe y.name
    x.description shouldBe y.description
    x.dataStructureId shouldBe y.dataStructureId
    x.formStructure shouldBe y.formStructure
    x.modifyAttr.createdBy shouldBe y.userId
    x.modifyAttr.updatedBy shouldBe None
  }
  "A FormTemplatesRepositoryActor" when receive {
    "CreateCmd" must {
      "create new form template in repository" in {
        for {
          d1 <- generateUpdateFormTemplate()
          d2 <- generateUpdateFormTemplate()
          a <- newFormTemplatesRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          z <- ask(a, GetAll).mapTo[FormTemplateMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z should have size 2
          z should contain key d1.id
          z should contain key d2.id
          assertFormTemplate(z(d1.id), d1)
          assertFormTemplate(z(d2.id), d2)
        }
      }
    }
    "UpdateCmd" must {
      "update form template in repository" in {
        for {
          d1 <- generateUpdateFormTemplate()
          d2 <- generateUpdateFormTemplate(d1.id)
          a <- newFormTemplatesRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, UpdateCmd(d2))
          z <- ask(a, GetById(d1.id)).mapTo[FormTemplateOpt].map(_.x.get)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z.id shouldBe d1.id
          z.name shouldBe d2.name
          z.description shouldBe d2.description
          z.dataStructureId shouldBe d2.dataStructureId
          z.formStructure shouldBe d2.formStructure
          z.modifyAttr.createdBy shouldBe d1.userId
          z.modifyAttr.updatedBy shouldBe Option(d2.userId)
        }
      }
      "not update when form template not exist" in {
        for {
          d1 <- generateUpdateFormTemplate()
          a <- newFormTemplatesRepositoryActor()
          z <- ask(a, UpdateCmd(d1))
        } yield z shouldBe NotFound
      }
    }
    "DeleteCmd" must {
      "delete form template in repository" in {
        for {
          d1 <- generateUpdateFormTemplate()
          d2 <- generateUpdateFormTemplate()
          a <- newFormTemplatesRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          z <- ask(a, DeleteCmd(d2.id))
          j <- ask(a, GetAll).mapTo[FormTemplateMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z shouldBe Done
          j should have size 1
          j should contain key d1.id
        }
      }
      "not delete when form template not exist" in {
        for {
          d1 <- generateUpdateFormTemplate()
          a <- newFormTemplatesRepositoryActor()
          z <- ask(a, DeleteCmd(d1.id))
        } yield z shouldBe NotFound
      }
    }
  }
}