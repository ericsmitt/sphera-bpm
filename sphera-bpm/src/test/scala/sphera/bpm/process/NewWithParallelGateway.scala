package sphera.bpm.process

import akka.testkit.TestKit
import sphera.bpm.BpmBaseSpec
import sphera.bpm.Implicits._
import sphera.bpm.lang._
import sphera.bpm.process.actor.ProcessState
import sphera.bpm.storage.definition.{ DefStorage, IntDef }
import sphera.bpm.storage.runnable.RunnableStorage
import sphera.core.test.PersistenceSpec

trait NewWithParallelGateway extends BpmBaseSpec { _: PersistenceSpec with TestKit =>
  val scriptTaskOperationAId = "scriptA"
  val scriptTaskOperationA1Id = "scriptA1"
  val scriptTaskOperationA2Id = "scriptA2"
  val scriptTaskOperationBId = "scriptB"
  val parallelGatewayOperationAId = "parallelGatewayA"
  val parallelGatewayOperationA1Id = "parallelGatewayA1"
  val parallelGatewayOperationBId = "parallelGatewayB"

  val incAssignment = Assignment("i", Plus(Variable("i"), DecimalLiteral(1)))

  val startOperation = StartOperationDef(
    startOperationId,
    startOperationId,
    List(parallelGatewayOperationAId))

  val parallelGatewayOperationA = ParallelGatewayOperationDef(
    parallelGatewayOperationAId,
    parallelGatewayOperationAId,
    List(startOperationId),
    List(scriptTaskOperationAId, scriptTaskOperationBId))

  val scriptTaskOperationA = ScriptTaskOperationDef(
    scriptTaskOperationAId,
    scriptTaskOperationAId,
    List(parallelGatewayOperationAId),
    List(parallelGatewayOperationA1Id),
    List(incAssignment),
    List.empty)

  val parallelGatewayOperationA1 = ParallelGatewayOperationDef(
    parallelGatewayOperationA1Id,
    parallelGatewayOperationA1Id,
    List(scriptTaskOperationAId),
    List(scriptTaskOperationA1Id, scriptTaskOperationA2Id))

  val scriptTaskOperationA1 = ScriptTaskOperationDef(
    scriptTaskOperationA1Id,
    scriptTaskOperationA1Id,
    List(parallelGatewayOperationA1Id),
    List(parallelGatewayOperationBId),
    List(incAssignment),
    List.empty)

  val scriptTaskOperationA2 = ScriptTaskOperationDef(
    scriptTaskOperationA2Id,
    scriptTaskOperationA2Id,
    List(parallelGatewayOperationA1Id),
    List(parallelGatewayOperationBId),
    List(incAssignment),
    List.empty)

  val scriptTaskOperationB = ScriptTaskOperationDef(
    scriptTaskOperationBId,
    scriptTaskOperationBId,
    List(parallelGatewayOperationAId),
    List(parallelGatewayOperationBId),
    List(incAssignment),
    List.empty)

  val parallelGatewayOperationB = ParallelGatewayOperationDef(
    parallelGatewayOperationBId,
    parallelGatewayOperationBId,
    List(scriptTaskOperationA1Id, scriptTaskOperationA2Id, scriptTaskOperationBId),
    List(endOperationId))

  val endOperation = EndOperationDef(
    endOperationId,
    endOperationId,
    List(parallelGatewayOperationBId))

  val state = ProcessState(
    ProcessDef(
      DefStorage.emptyRootDef(),
      Set(
        startOperation,
        scriptTaskOperationA,
        scriptTaskOperationA1,
        scriptTaskOperationA2,
        scriptTaskOperationB,
        parallelGatewayOperationA,
        parallelGatewayOperationA1,
        parallelGatewayOperationB,
        endOperation)),
    RunnableStorage(DefStorage.emptyRootDef().addDef(IntDef(
      name = "i",
      description = None,
      index = None,
      value = 0,
      path = "i"))))
}