package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.masterdata.actor.CalendarTypesRepositoryActor._
import sphera.core.test.PersistenceSpec

class CalendarTypesRepositoryActorSpec extends TestKit(ActorSystem("CalendarTypesRepositorySpec"))
  with PersistenceSpec
  with NewCalendarTypesRepository {
  "A CalendarTypesRepositoryActor" when receive {
    "CreateCmd" must {
      "create new defStorageActor structure in repository" in {
        //        val calendarId1 = UUID.randomUUID()
        //        val calendarId2 = UUID.randomUUID()
        val user1 = UUID.randomUUID()
        for {
          d1 <- generateUpdateCalendarType()
          d2 <- generateUpdateCalendarType()
          a <- newCalendarTypesRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          b <- ask(a, UpdateUsersCalendarTypesMapCmd(user1, d1.id))
          c <- ask(a, UpdateUsersCalendarTypesMapCmd(UUID.randomUUID(), d2.id))
          z <- ask(a, GetAll).mapTo[CalendarTypeMap].map(_.x)
          l <- ask(a, GetUsersCalendarType).mapTo[UsersCalendarTypes]
          f <- ask(a, GetUsersCalendarTypeId(user1)).mapTo[UsersCalendarType]
          u <- ask(a, GetUsersCalendarTypeId(UUID.randomUUID()))
          q <- ask(a, DeleteCmd(d2.id))
          p <- ask(a, GetUsersCalendarType).mapTo[UsersCalendarTypes]

        } yield {
          x shouldBe Done
          y shouldBe Done
          b shouldBe Done
          c shouldBe Done
          z should have size 2
          z should contain key d1.id
          z should contain key d2.id
          z(d1.id).name shouldBe d1.name
          z(d1.id).description shouldBe d1.description
          z(d1.id).deviations shouldBe d1.deviations
          z(d1.id).modifyAttr.createdBy shouldBe d1.userId
          z(d1.id).modifyAttr.updatedBy shouldBe None
          l.userCalendarTypeMap.size shouldBe 2
          f.calendarId shouldBe d1.id
          u shouldBe NotFound
          p.userCalendarTypeMap.size shouldBe 1
        }
      }
    }
    "UpdateCmd" must {
      "update defStorageActor structure in repository" in {
        for {
          d1 <- generateUpdateCalendarType()
          d2 <- generateUpdateCalendarType(d1.id)
          a <- newCalendarTypesRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, UpdateCmd(d2))
          z <- ask(a, GetById(d1.id)).mapTo[CalendarTypeOpt].map(_.x.get)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z.id shouldBe d1.id
          z.name shouldBe d2.name
          z.description shouldBe d2.description
          z.deviations shouldBe d2.deviations
          z.modifyAttr.createdBy shouldBe d1.userId
          z.modifyAttr.updatedBy shouldBe Option(d2.userId)
        }
      }
      "not update when defStorageActor structure not exist" in {
        for {
          d1 <- generateUpdateCalendarType()
          a <- newCalendarTypesRepositoryActor()
          z <- ask(a, UpdateCmd(d1))
        } yield z shouldBe NotFound
      }
    }
    "DeleteCmd" must {
      "delete defStorageActor structure in repository" in {
        for {
          d1 <- generateUpdateCalendarType()
          d2 <- generateUpdateCalendarType()
          a <- newCalendarTypesRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          z <- ask(a, DeleteCmd(d2.id))
          b <- ask(a, GetAll).mapTo[CalendarTypeMap].map(_.x)
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
          d1 <- generateUpdateCalendarType()
          a <- newCalendarTypesRepositoryActor()
          z <- ask(a, DeleteCmd(d1.id))
        } yield z shouldBe NotFound
      }
    }
  }
}