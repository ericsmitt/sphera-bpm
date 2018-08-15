//defstorage sphera.bpm.process.actor
//
//import akka.actor.ActorSystem
//import akka.testkit.TestKit
//import sphera.bpm.process.actor.ProcessActor.Done
//import sphera.core.test.PersistenceSpec
//import org.scalatest.ParallelTestExecution
//
//class ProcessInstanceActorSpec extends TestKit(ActorSystem("ProcessInstanceActorSpec"))
//  with PersistenceSpec
//  with NewWithParallelGateway
//  with ParallelTestExecution {
//  "A ProcessActor" when receive {
//    "StartCmd" must {
//      "start processId" in {
//        for {
//          (_, a) <- newProcessA()
//          p2 <- startProcess(a)
//        } yield p2 shouldBe Done
//      }
//    }
//  }
//}