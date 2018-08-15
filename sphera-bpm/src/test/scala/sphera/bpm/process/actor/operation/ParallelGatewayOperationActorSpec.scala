//package annette.bpm.process.actor.operation
//
//import akka.actor.ActorSystem
//import akka.testkit.TestKit
//import annette.core.test.PersistenceSpec
//
//class ParallelGatewayOperationActorSpec extends TestKit(ActorSystem("ParallelGatewayOperationActorSpec"))
//  with PersistenceSpec
//  with NewOperation {
//  "A ParallelGatewayOperationActor" when {
//    "Converging" must {
//      "converge and activate one outgoing" in {
//        for {
//          (p, a) <- newParallelGatewayOperationA()
//          x <- activate(a, incomingsA)
//        } yield expectActivate(p, outgoingA)
//      }
//    }
//    "Mixed" must {
//      "first converge and then to diverge" in {
//        for {
//          (p, a) <- newParallelGatewayOperationB()
//          x <- activate(a, incomingsA)
//        } yield expectActivate(p, outgoingA)
//      }
//    }
//    "recover" must {
//      "converge and activate one outgoing" in {
//        val id = generateId
//        for {
//          (p, a) <- newParallelGatewayOperationA(id)
//          _ <- activate(a, incomingsA.tail)
//          _ <- kill(a)
//          (p, a) <- newParallelGatewayOperationA(id)
//          _ <- activate(a, Seq(incomingsA.head))
//        } yield expectActivate(p, outgoingA)
//      }
//    }
//  }
//}