package sphera.bpm.process

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.Implicits._
import org.scalatest.Ignore

@Ignore
class CountPiSpec extends TestKit(ActorSystem("CountPiSpec")) with NewCountPi {
  "A process for count pi" must {
    "complete" in {
      val p = newTestProbe
        .withSubscriptionOnProcessEvents
        .withSubscriptionOnHistoryEvents

      for {
        bpm <- newBpm()
        r1 <- bpm.createProcessAndGetId(generateCreateProcessInfo(Option(state)))
        r2 <- bpm.startProcess(r1)
      } yield {
        fishForStartProcess(p)
        fishForActivate(p, exclusiveGatewayOperationId)
        fishForActivateOneOfN(p, List(scriptTaskOperationAId, scriptTaskOperationBId), i)
        fishForActivate(p, exclusiveGatewayOperationId)
        fishForActivate(p, scriptTaskOperationCId)
        fishForEndOperation(p)
        val pi = fixPrecision(fishForCreateHistoryGetDefStorage(p).getDefDecimalValue("pi"))
        info(s"Result PI $pi")
        pi - PI >= -0.0001 shouldBe true
      }
    }
  }
}