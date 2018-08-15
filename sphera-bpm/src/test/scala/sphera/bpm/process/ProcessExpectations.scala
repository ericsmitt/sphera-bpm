package sphera.bpm.process

import akka.testkit.{ TestKit, TestProbe }
import sphera.bpm.Implicits._
import sphera.bpm.process.Process.ActivatedEvt
import sphera.bpm.runnable.RunnableManager
import sphera.bpm.runnable.RunnableManager._
import sphera.core.test.PersistenceSpec
import org.scalatest.Assertion

import scala.concurrent.duration._

trait ProcessExpectations extends RunnableExpectations { _: PersistenceSpec with TestKit =>
  val startOperationId = "Start"
  val endOperationId = "End"

  def fishForStartProcess(p: TestProbe): Assertion = {
    fishForActivate(p, startOperationId)
  }

  def fishForEndOperation(p: TestProbe): Assertion = {
    fishForActivate(p, endOperationId, 1 minute)
  }

  def fishForActivate(p: TestProbe, op: Operation.BpmnId, max: Duration = 3 second): Assertion = {
    p.fishForSpecificMessage[ActivatedEvt](max, s"OperationActivatedEvt, op: $op") {
      case event: ActivatedEvt if event.to contains op => event
    }
    info(s"Activated operation [$op]")
    succeed
  }

  def fishForActivate(p: TestProbe, ops: List[Operation.BpmnId]): Assertion = {
    p.fishForSpecificMessage[ActivatedEvt](3 second, s"OperationActivatedEvt, ops: $ops") {
      case event: ActivatedEvt if event.to == ops => event
    }
    info(s"Activated operations [$ops]")
    succeed
  }

  def expectActivate(p1: TestProbe, p2: List[Operation.BpmnId]): Assertion = {
    p1.expectMsgType[ActivatedEvt].to shouldBe p2
    info(s"Activated operation [$p2]")
    succeed
  }

  def expectActivateOneOf(p: TestProbe, op: Set[Operation.BpmnId]): Assertion = {
    val id = p.expectMsgType[ActivatedEvt].to
    val x = op should contain(id)
    info(s"Activated operation [$id]")
    x
  }

  //  def fishForActivateOneOf(p1: TestProbe, p2: Set[Operation.BpmnId]): Assertion = {
  //    p1.fishForSpecificMessage[ActivatedEvt](1 second, s"OperationActivatedEvt") {
  //      case event: ActivatedEvt if p2 contains event.to => event
  //    }
  //    succeed
  //  }

  def fishForActivateOneOfN(p1: TestProbe, p2: List[Operation.BpmnId], p3: Int): Assertion = {
    for (i <- 1 to p3) yield p1.fishForSpecificMessage[ActivatedEvt](1 second, s"Activate operation oneOf [$p2]") {
      case event @ ActivatedEvt(x, _) if x.intersect(p2).nonEmpty => event
    }
    info(s"Activated $p3 times oneOf [$p2]")
    succeed
  }

  def fishForActivateN(p1: TestProbe, p2: Operation.BpmnId, p3: Int): Assertion = {
    for (i <- 1 to p3) yield p1.fishForSpecificMessage[ActivatedEvt](1 minute, s"Activate operation [$p2]") {
      case event @ ActivatedEvt(x, _) if x contains p2 => event
    }
    info(s"Activated $p3 times operation [$p2]")
    succeed
  }

  def fishForCompleteProcess(p: TestProbe): Assertion = {
    fishForEndOperation(p)
    p.expectMsgType[RunnableManager.DeletedRunnableInfoEvt]
    succeed
  }

  def fishForCompleteProcess(p: TestProbe, processId: Process.Id): Assertion = {
    fishForEndOperation(p)
    p.fishForSpecificMessage[DeletedRunnableInfoEvt](3 second, s"DeletedRunnableInfoEvt, subProcessId: $processId") {
      case event: DeletedRunnableInfoEvt if event.runnableId == processId => event
    }
    info(s"Process completed [$processId]")
    succeed
  }

  implicit class SubscribedOnProcessEventsTestProbe(p: TestProbe) {
    def withSubscriptionOnProcessEvents: TestProbe = {
      system.eventStream.subscribe(p.ref, classOf[Process.Event])
      p
    }
  }
}
