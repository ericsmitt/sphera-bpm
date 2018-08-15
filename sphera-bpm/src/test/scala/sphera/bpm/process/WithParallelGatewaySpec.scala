package sphera.bpm.process

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import sphera.bpm.BpmBaseSpec
import sphera.bpm.Implicits._
import sphera.bpm.model.ImplicitModifier
import sphera.core.test.PersistenceSpec
import org.scalatest.ParallelTestExecution

import scala.concurrent.Future

class WithParallelGatewaySpec extends TestKit(ActorSystem("WithParallelGatewaySpec")) with NewWithParallelGateway {
  "A process with parallel gateway" must {
    "complete" in {
      val p = newTestProbe.withSubscriptionOnProcessEvents
      def expectations(p: TestProbe) = Future {
        fishForStartProcess(p)
        fishForActivate(p, parallelGatewayOperationAId)
        expectActivate(p, List(scriptTaskOperationAId, scriptTaskOperationBId))
        fishForActivateN(p, parallelGatewayOperationBId, 3)
        fishForEndOperation(p)
      }
      for {
        bpm <- newBpm()
        r1 <- bpm.createProcessAndGetId(generateCreateProcessInfo(Option(state)))
        r2 <- bpm.startProcess(r1)
        r3 <- expectations(p)
        r4 <- bpm.getProcessJson(r1, "value.i.value")
      } yield r4.asNumber.get.toInt.get should be >= 1
    }
  }
}