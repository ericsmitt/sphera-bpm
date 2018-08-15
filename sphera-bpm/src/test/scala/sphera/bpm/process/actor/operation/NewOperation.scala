//package annette.bpm.process.actor.operation
//
//import akka.actor.{ Actor, ActorRef }
//import akka.testkit.{ TestKit, TestProbe }
//import annette.bpm.process.Operation
//import annette.bpm.process.Process
//import annette.core.test.PersistenceSpec
//import org.scalatest._
//
//import scala.concurrent.Future
//import scala.concurrent.duration._
//
//trait NewOperation { _: PersistenceSpec with TestKit =>
//  val incomingA: String = generateId
//  val incomingB: String = generateId
//  val incomingC: String = generateId
//  val incomingsA = Seq(
//    incomingA,
//    incomingB,
//    incomingC)
//
//  val outgoingA: String = generateId
//  val outgoingsA = Seq(
//    outgoingA)
//
//  def expectActivate(p: TestProbe, id: Operation.Id): Assertion = {
//    val x = p.expectMsgType[Process.ActivateCmd]
//    x.to shouldBe id
//    x.from should not be empty
//  }
//
//  def expectActivate(p: TestProbe, ids: Seq[Operation.Id]): Assertion = {
//    for (i <- 1 to ids.size) yield p.fishForSpecificMessage[ProcessActor.ActivateCmd](100 milliseconds, "activate")({
//      case cmd @ ProcessActor.ActivateCmd(x, _) if ids contains x => cmd
//    })
//    succeed
//  }
//
//  def activate(a: ActorRef, incoming: Seq[Operation.Id]): Future[Unit] = Future {
//    for (x <- incoming) yield a ! OperationActor.ActivateCmd(Option(x), Definitions.empty, RoleDefs.empty)
//  }
//
//  def generateOperationId = s"operation-$generateInt"
//
//  def newParallelGatewayOperation(id: Operation.Id, incoming: Seq[Operation.Id], outgoing: Seq[Operation.Id]): Future[(TestProbe, ActorRef)] = Future {
//    val p = new TestProbe(system)
//    p -> p.childActorOf(ParallelGatewayOperationActor.props(id, s"path-$id", s"n-$id", incoming, outgoing), id)
//  }
//
//  def newParallelGatewayOperationA(id: Operation.Id = generateOperationId): Future[(TestProbe, ActorRef)] =
//    newParallelGatewayOperation(id, incomingsA, Seq(outgoingA))
//
//  def newParallelGatewayOperationB(id: Operation.Id = generateOperationId): Future[(TestProbe, ActorRef)] =
//    newParallelGatewayOperation(id, incomingsA, outgoingsA)
//
//  class TestOperationActor extends Actor {
//    def receive: Receive = {
//      case _: OperationActor.ActivateCmd =>
//    }
//  }
//}