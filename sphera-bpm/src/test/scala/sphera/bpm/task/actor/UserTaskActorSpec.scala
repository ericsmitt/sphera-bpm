//package sphera.bpm.task.actor
//
//import akka.actor.ActorSystem
//import akka.testkit.TestKit
//import sphera.core.test.PersistenceSpec
//import org.scalatest.ParallelTestExecution
//
//class UserTaskActorSpec extends TestKit(ActorSystem("UserTaskActorSpec"))
//  with PersistenceSpec
//  with NewUserTask
//  with ParallelTestExecution {
//  "A UserTaskActor" when receive {
//    "UpdateDataCmd" must {
//      "update data" in {
//        for {
//          a <- newUserTaskA()
//          x <- updateData(a, dataA)
//          y <- getData(a)
//        } yield {
//          x shouldBe DefStorage.Done
//          y.repr should not be empty
//        }
//      }
//    }
//  }
//}