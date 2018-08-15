package sphera.bpm.process

import akka.testkit.TestKit
import sphera.bpm.BpmBaseSpec
import sphera.bpm.Implicits._
import sphera.bpm.lang._
import sphera.bpm.process.actor.ProcessState
import sphera.bpm.storage.definition.{DecimalDef, DefStorage}
import sphera.bpm.storage.runnable.RunnableStorage
import sphera.core.test.PersistenceSpec

trait NewCountPi extends BpmBaseSpec { _: PersistenceSpec with TestKit =>
    // PI counter
    val i = 10000
    val PI: BigDecimal = fixPrecision(BigDecimal(Math.PI))

    val scriptTaskOperationAId = "scriptA"
    val scriptTaskOperationBId = "scriptB"
    val scriptTaskOperationCId = "scriptC"
    val exclusiveGatewayOperationId = "exclusiveGateway"

  val startOperation = StartOperationDef(
    startOperationId,
    startOperationId,
    List(exclusiveGatewayOperationId))


    val exprA = Multiply(DecimalLiteral(2), Variable("i"))
    val exprB = Plus(exprA,  DecimalLiteral(1))
    val exprC = Divide(DecimalLiteral(1), exprB)
    val incAssignment = Assignment("i", Plus(Variable("i"), DecimalLiteral(1)))

    val scriptTaskOperationA = ScriptTaskOperationDef(
      scriptTaskOperationAId,
      scriptTaskOperationAId,
      List(exclusiveGatewayOperationId),
      List(exclusiveGatewayOperationId),
      List(Assignment("pi", Plus(Variable("pi"), exprC)), incAssignment),
      List.empty
    )

    val scriptTaskOperationB = ScriptTaskOperationDef(
      scriptTaskOperationBId,
      scriptTaskOperationBId,
      List(exclusiveGatewayOperationId),
      List(exclusiveGatewayOperationId),
      List(Assignment("pi", Minus(Variable("pi"), exprC)), incAssignment),
      List.empty
    )

  val exclusiveGatewayOperation = ExclusiveGatewayOperationDef(
    exclusiveGatewayOperationId,
    exclusiveGatewayOperationId,
    List(startOperationId, scriptTaskOperationAId, scriptTaskOperationBId),
    List(scriptTaskOperationAId, scriptTaskOperationBId, scriptTaskOperationCId),
    List(
      CaseBranch("if i < p3 and i % 2 == 0", And(LowerThan(Variable("i"), Variable("p3")), Equal(Remainder(Variable("i"), DecimalLiteral(2)), DecimalLiteral(0))), scriptTaskOperationAId),
      CaseBranch("if i < p3", LowerThan(Variable("i"), Variable("p3")), scriptTaskOperationBId),
      OtherwiseBranch("if >= p3", scriptTaskOperationCId))
  )

  val scriptTaskOperationC = ScriptTaskOperationDef(
      scriptTaskOperationCId,
      scriptTaskOperationCId,
      List(exclusiveGatewayOperationId),
      List(endOperationId),
      List(Assignment("pi", Multiply(Variable("pi"), DecimalLiteral(4)))),
      List.empty
  )

  val endOperation = EndOperationDef(
    endOperationId,
    endOperationId,
    List(scriptTaskOperationCId)
  )

  val state = ProcessState(
    ProcessDef(
      DefStorage.emptyRootDef(),
      Set(
        startOperation,
        scriptTaskOperationA,
        scriptTaskOperationB,
        scriptTaskOperationC,
        exclusiveGatewayOperation,
        endOperation
      )
    ),
    RunnableStorage(DefStorage.emptyRootDef().addDef(List(
      DecimalDef("pi", None, None, 0, "pi"),
      DecimalDef("p3", None, None, i, "p3"),
      DecimalDef("i", None, None, 0, "i"),
    )))
  )

  def fixPrecision(p2: BigDecimal): BigDecimal = p2.setScale(11, BigDecimal.RoundingMode.HALF_UP)
}