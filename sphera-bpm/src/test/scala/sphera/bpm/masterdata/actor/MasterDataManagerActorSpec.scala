package sphera.bpm.masterdata.actor

import akka.actor.ActorSystem
import akka.actor.SupervisorStrategy._
import akka.testkit.{ TestActorRef, TestKit }
import sphera.bpm.BpmBaseSpec

class MasterDataManagerActorSpec extends TestKit(ActorSystem("MasterDataManagerActorSpec")) with BpmBaseSpec {
  "A supervisor strategy of the MasterDataSupervisorActor" when receive {
    "ArithmeticException" must {
      "resume actorRef " in {
        newBpm() map { bpm =>
          val id = generateActorId
          val supervisor = TestActorRef[MasterDataManagerActor](MasterDataManagerActor.props(id, bpm), id.name)
          val strategy = supervisor.underlyingActor.supervisorStrategy.decider
          strategy(new ArithmeticException) shouldBe Resume
        }
      }
    }
    "NullPointerException" must {
      "restart actorRef " in {
        newBpm() map { bpm =>
          val id = generateActorId
          val supervisor = TestActorRef[MasterDataManagerActor](MasterDataManagerActor.props(id, bpm), id.name)
          val strategy = supervisor.underlyingActor.supervisorStrategy.decider
          strategy(new NullPointerException) shouldBe Restart
        }
      }
    }
    "IllegalArgumentException" must {
      "resume actorRef " in {
        newBpm() map { bpm =>
          val id = generateActorId
          val supervisor = TestActorRef[MasterDataManagerActor](MasterDataManagerActor.props(id, bpm), id.name)
          val strategy = supervisor.underlyingActor.supervisorStrategy.decider
          strategy(new IllegalArgumentException) shouldBe Resume
        }
      }
    }
    "Exception" must {
      "restart actorRef " in {
        newBpm() map { bpm =>
          val id = generateActorId
          val supervisor = TestActorRef[MasterDataManagerActor](MasterDataManagerActor.props(id, bpm), id.name)
          val strategy = supervisor.underlyingActor.supervisorStrategy.decider
          strategy(new Exception) shouldBe Restart
        }
      }
    }
  }
  "A MasterDataSupervisorActor" when receive {
    "DataStructuresRepositoryActor.Command" must {
      "forward it" in {
        for {
          u <- generateUpdateDataStructure()
          a <- newMasterDataManagerActor
          x <- ask(a, DataStructuresRepositoryActor.CreateCmd(u))
          y <- ask(a, DataStructuresRepositoryActor.GetAll)
            .mapTo[DataStructuresRepositoryActor.DataStructureMap]
            .map(_.x)
        } yield {
          x shouldBe DataStructuresRepositoryActor.Done
          y should have size 1
        }
      }
    }
    "CharacteristicRepositoryActor.Command" must {
      "forward it" in {
        for {
          u <- generateUpdateCharacteristic()
          a <- newMasterDataManagerActor
          x <- ask(a, CharacteristicsRepositoryActor.CreateCmd(u))
          y <- ask(a, CharacteristicsRepositoryActor.GetAll)
            .mapTo[CharacteristicsRepositoryActor.CharacteristicMap]
            .map(_.x)
        } yield {
          x shouldBe CharacteristicsRepositoryActor.Done
          y should have size 1
        }
      }
    }
    "FileTypeRepositoryActor.Command" must {
      "forward it" in {
        for {
          u <- generateUpdateFileType()
          a <- newMasterDataManagerActor
          x <- ask(a, FileTypesRepositoryActor.CreateCmd(u))
          y <- ask(a, FileTypesRepositoryActor.GetAll)
            .mapTo[FileTypesRepositoryActor.FileTypeMap]
            .map(_.x)
        } yield {
          x shouldBe FileTypesRepositoryActor.Done
          y should have size 1
        }
      }
    }
    "FormsRepositoryActor.Command" must {
      "forward it" in {
        for {
          u <- generateUpdateFormTemplate()
          a <- newMasterDataManagerActor
          x <- ask(a, FormTemplatesRepositoryActor.CreateCmd(u))
          y <- ask(a, FormTemplatesRepositoryActor.GetAll)
            .mapTo[FormTemplatesRepositoryActor.FormTemplateMap]
            .map(_.x)
        } yield {
          x shouldBe FormTemplatesRepositoryActor.Done
          y should have size 1
        }
      }
    }
    "RoleTypeRepositoryActor.Command" must {
      "forward it" in {
        for {
          u <- generateUpdateRoleType()
          a <- newMasterDataManagerActor
          x <- ask(a, RoleTypesRepositoryActor.CreateCmd(u))
          y <- ask(a, RoleTypesRepositoryActor.GetAll)
            .mapTo[RoleTypesRepositoryActor.RoleTypeMap]
            .map(_.x)
        } yield {
          x shouldBe RoleTypesRepositoryActor.Done
          y should have size 1
        }
      }
    }
    "ProcessesRepositoryActor.Command" must {
      "forward it" in {
        for {
          u <- generateUpdateProcessTemplate()
          a <- newMasterDataManagerActor
          x <- ask(a, ProcessTemplatesRepositoryActor.CreateProcessTemplateCmd(u))
          y <- ask(a, ProcessTemplatesRepositoryActor.GetProcessTemplateAll)
            .mapTo[ProcessTemplatesRepositoryActor.ProcessTemplateMap]
            .map(_.x)
        } yield {
          x shouldBe ProcessTemplatesRepositoryActor.Done
          y should have size 1
        }
      }
    }
    "ProjectTemplateRepositoryActor.Command" must {
      "forward it" in {
        for {
          u <- generateUpdateProjectTemplate()
          a <- newMasterDataManagerActor
          x <- ask(a, ProjectTemplatesRepositoryActor.CreateCmd(u))
          y <- ask(a, ProjectTemplatesRepositoryActor.GetAll)
            .mapTo[ProjectTemplatesRepositoryActor.ProjectTemplateMap]
            .map(_.x)
        } yield {
          x shouldBe ProjectTemplatesRepositoryActor.Done
          y should have size 1
        }
      }
    }
    "CalendarTypeRepositoryActor.Command" must {
      "forward it" in {
        for {
          u <- generateUpdateCalendarType()
          a <- newMasterDataManagerActor
          x <- ask(a, CalendarTypesRepositoryActor.CreateCmd(u))
          y <- ask(a, CalendarTypesRepositoryActor.GetAll)
            .mapTo[CalendarTypesRepositoryActor.CalendarTypeMap]
            .map(_.x)
        } yield {
          x shouldBe CalendarTypesRepositoryActor.Done
          y should have size 1
        }
      }
    }
  }
}