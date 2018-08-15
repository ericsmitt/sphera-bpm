//package sphera.bpm.serialization
//
//import sphera.bpm.lang._
//import sphera.bpm.process.Assignment
//import sphera.bpm.process.Operation.Assignments
//import sphera.bpm.process.model._
//import sphera.bpm.serializer.proto.lang._
//import sphera.bpm.serializer.proto.process._
//
//trait LangConverters extends DefinitionConverters with ModelConverters {
//  import Implicits._
//
//  def toExpression(x: Expression): ExpressionV1 = {
//    x match {
//      case y: Equal =>
//        ExpressionV1.defaultInstance.withOpt1(
//          EqualV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: NotEqual =>
//        ExpressionV1.defaultInstance.withOpt2(
//          NotEqualV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: GreaterThan =>
//        ExpressionV1.defaultInstance.withOpt3(
//          GreaterThanV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: LowerThan =>
//        ExpressionV1.defaultInstance.withOpt4(
//          LowerThanV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: GreaterThanOrEqual =>
//        ExpressionV1.defaultInstance.withOpt5(
//          GreaterThanOrEqualV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: LowerThanOrEqual =>
//        ExpressionV1.defaultInstance.withOpt6(
//          LowerThanOrEqualV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: Not =>
//        ExpressionV1.defaultInstance.withOpt7(
//          NotV1(
//            ex = toExpression(y.ex)))
//      case y: Or =>
//        ExpressionV1.defaultInstance.withOpt8(
//          OrV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: And =>
//        ExpressionV1.defaultInstance.withOpt9(
//          AndV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: True =>
//        ExpressionV1.defaultInstance.withOpt10(
//          TrueV1())
//      case y: False =>
//        ExpressionV1.defaultInstance.withOpt11(
//          FalseV1())
//      case y: Plus =>
//        ExpressionV1.defaultInstance.withOpt12(
//          PlusV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: Minus =>
//        ExpressionV1.defaultInstance.withOpt13(
//          MinusV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: Divide =>
//        ExpressionV1.defaultInstance.withOpt14(
//          DivideV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: Multiply =>
//        ExpressionV1.defaultInstance.withOpt15(
//          MultiplyV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: Remainder =>
//        ExpressionV1.defaultInstance.withOpt16(
//          RemainderV1(
//            ex1 = toExpression(y.ex1),
//            ex2 = toExpression(y.ex2)))
//      case y: Variable =>
//        ExpressionV1.defaultInstance.withOpt17(
//          VariableV1(
//            name = y.name))
//      case y: StringLiteral =>
//        ExpressionV1.defaultInstance.withOpt18(
//          StringLiteralV1(
//            value = y.value))
//      case y: DecimalLiteral =>
//        ExpressionV1.defaultInstance.withOpt19(
//          DecimalLiteralV1(
//            value = y.value.toString()))
//    }
//  }
//
//  def toExpressions(x: Seq[Expression]): Seq[ExpressionV1] = {
//    x.map(toExpression)
//  }
//
//  def fromExpression(x: ExpressionV1): Expression = {
//    val opt1 = x.operationOneof.opt1
//    val opt2 = x.operationOneof.opt2
//    val opt3 = x.operationOneof.opt3
//    val opt4 = x.operationOneof.opt4
//    val opt5 = x.operationOneof.opt5
//    val opt6 = x.operationOneof.opt6
//    val opt7 = x.operationOneof.opt7
//    val opt8 = x.operationOneof.opt8
//    val opt9 = x.operationOneof.opt9
//    val opt10 = x.operationOneof.opt10
//    val opt11 = x.operationOneof.opt11
//    val opt12 = x.operationOneof.opt12
//    val opt13 = x.operationOneof.opt13
//    val opt14 = x.operationOneof.opt14
//    val opt15 = x.operationOneof.opt15
//    val opt16 = x.operationOneof.opt16
//    val opt17 = x.operationOneof.opt17
//    val opt18 = x.operationOneof.opt18
//    val opt19 = x.operationOneof.opt19
//    Seq(opt1, opt2, opt3, opt4, opt5, opt6, opt7, opt8, opt9, opt10, opt11,
//      opt12, opt13, opt14, opt15, opt16, opt17, opt18, opt19).flatten.head match {
//      case y: EqualV1 =>
//        Equal(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: NotEqualV1 =>
//        NotEqual(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: GreaterThanV1 =>
//        GreaterThan(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: LowerThanV1 =>
//        LowerThan(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: GreaterThanOrEqualV1 =>
//        GreaterThanOrEqual(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: LowerThanOrEqualV1 =>
//        LowerThanOrEqual(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: NotV1 =>
//        Not(
//          ex = fromExpression(y.ex))
//      case y: OrV1 =>
//        Or(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: AndV1 =>
//        And(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: TrueV1 =>
//        True()
//      case y: FalseV1 =>
//        False()
//      case y: PlusV1 =>
//        Plus(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: MinusV1 =>
//        Minus(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: DivideV1 =>
//        Divide(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: MultiplyV1 =>
//        Multiply(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: RemainderV1 =>
//        Remainder(
//          ex1 = fromExpression(y.ex1),
//          ex2 = fromExpression(y.ex2))
//      case y: VariableV1 =>
//        Variable(
//          name = y.name)
//      case y: StringLiteralV1 =>
//        StringLiteral(
//          value = y.value)
//      case y: DecimalLiteralV1 =>
//        DecimalLiteral(
//          value = y.value)
//    }
//  }
//
//  def fromExpressions(x: Seq[ExpressionV1]): List[Expression] = {
//    x.map(fromExpression).toList
//  }
//
//  implicit def toAssignment(x: Assignment): AssignmentV1 = {
//    AssignmentV1(
//      path = x.path,
//      expr = toExpression(x.expr))
//  }
//
//  implicit def fromAssignment(x: AssignmentV1): Assignment = {
//    Assignment(
//      path = x.path,
//      expr = fromExpression(x.expr))
//  }
//
//  implicit def toAssignments(x: Assignments): Seq[AssignmentV1] = {
//    x.map(toAssignment)
//  }
//
//  implicit def fromAssignments(x: Seq[AssignmentV1]): Assignments = {
//    x.map(fromAssignment).toList
//  }
//
//}