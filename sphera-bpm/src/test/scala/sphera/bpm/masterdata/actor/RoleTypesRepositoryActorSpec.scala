package sphera.bpm.masterdata.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.masterdata.actor.RoleTypesRepositoryActor._
import sphera.core.test.PersistenceSpec

class RoleTypesRepositoryActorSpec extends TestKit(ActorSystem("RoleTypesRepositoryActorSpec"))
  with PersistenceSpec
  with NewRoleTypesRepository {

  "A RoleTypesRepositoryActor" when receive {
    "CreateCmd" must {
      "create new role in repository" in {
        for {
          d1 <- generateUpdateRoleType()
          d2 <- generateUpdateRoleType()
          a <- newRoleTypesRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          z <- ask(a, GetAll).mapTo[RoleTypeMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z should have size 2
          z should contain key d1.id
          z should contain key d2.id
          z(d1.id).name shouldBe d1.name
          z(d1.id).description shouldBe d1.description
          z(d1.id).users shouldBe d1.users
          z(d1.id).modifyAttr.createdBy shouldBe d1.userId
          z(d1.id).modifyAttr.updatedBy shouldBe None
        }
      }
    }
    "UpdateCmd" must {
      "update role in repository" in {
        for {
          d1 <- generateUpdateRoleType()
          d2 <- generateUpdateRoleType(d1.id)
          a <- newRoleTypesRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, UpdateCmd(d2))
          z <- ask(a, GetById(d1.id)).mapTo[RoleTypeOpt].map(_.x.get)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z.id shouldBe d1.id
          z.name shouldBe d2.name
          z.description shouldBe d2.description
          z.users shouldBe d2.users
          z.modifyAttr.createdBy shouldBe d1.userId
          z.modifyAttr.updatedBy shouldBe Option(d2.userId)
        }
      }
      "not update when role not exist" in {
        for {
          d1 <- generateUpdateRoleType()
          a <- newRoleTypesRepositoryActor()
          z <- ask(a, UpdateCmd(d1))
        } yield z shouldBe NotFound
      }
    }
    "DeleteCmd" must {
      "delete role in repository" in {
        for {
          d1 <- generateUpdateRoleType()
          d2 <- generateUpdateRoleType()
          a <- newRoleTypesRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          z <- ask(a, DeleteCmd(d2.id))
          b <- ask(a, GetAll).mapTo[RoleTypeMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z shouldBe Done
          b should have size 1
          b should contain key d1.id
        }
      }
      "not delete when role not exist" in {
        for {
          d1 <- generateUpdateRoleType()
          a <- newRoleTypesRepositoryActor()
          z <- ask(a, DeleteCmd(d1.id))
        } yield z shouldBe NotFound
      }
    }
  }
}
