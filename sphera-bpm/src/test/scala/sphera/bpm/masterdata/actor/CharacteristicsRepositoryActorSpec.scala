package sphera.bpm.masterdata.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.core.test.PersistenceSpec

class CharacteristicsRepositoryActorSpec extends TestKit(ActorSystem("CharacteristicRepositorySpec"))
  with PersistenceSpec
  with NewCharacteristicRepository {
  import CharacteristicsRepositoryActor._

  "A CharacteristicRepositoryActor" when receive {
    "CreateCmd" must {
      "create new defStorageActor structure in repository" in {
        for {
          d1 <- generateUpdateCharacteristic()
          d2 <- generateUpdateCharacteristic()
          a <- newCharacteristicRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          z <- ask(a, GetAll).mapTo[CharacteristicMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z should have size 2
          z should contain key d1.id
          z should contain key d2.id
          z(d1.id).name shouldBe d1.name
          z(d1.id).description shouldBe d1.description
          z(d1.id).values shouldBe d1.values
          z(d1.id).modifyAttr.createdBy shouldBe d1.userId
          z(d1.id).modifyAttr.updatedBy shouldBe None
        }
      }
    }
    "UpdateCmd" must {
      "update defStorageActor structure in repository" in {
        for {
          d1 <- generateUpdateCharacteristic()
          d2 <- generateUpdateCharacteristic(d1.id)
          a <- newCharacteristicRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, UpdateCmd(d2))
          z <- ask(a, GetById(d1.id)).mapTo[CharacteristicOpt].map(_.x.get)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z.id shouldBe d1.id
          z.name shouldBe d2.name
          z.description shouldBe d2.description
          z.values shouldBe d2.values
          z.modifyAttr.createdBy shouldBe d1.userId
          z.modifyAttr.updatedBy shouldBe Option(d2.userId)
        }
      }
      "not update when defStorageActor structure not exist" in {
        for {
          d1 <- generateUpdateCharacteristic()
          a <- newCharacteristicRepositoryActor()
          z <- ask(a, UpdateCmd(d1))
        } yield z shouldBe NotFound
      }
    }
    "DeleteCmd" must {
      "delete defStorageActor structure in repository" in {
        for {
          d1 <- generateUpdateCharacteristic()
          d2 <- generateUpdateCharacteristic()
          a <- newCharacteristicRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          z <- ask(a, DeleteCmd(d2.id))
          b <- ask(a, GetAll).mapTo[CharacteristicMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z shouldBe Done
          b should have size 1
          b should contain key d1.id
        }
      }
      "not delete when defStorageActor structure not exist" in {
        for {
          d1 <- generateUpdateCharacteristic()
          a <- newCharacteristicRepositoryActor()
          z <- ask(a, DeleteCmd(d1.id))
        } yield z shouldBe NotFound
      }
    }
  }
}