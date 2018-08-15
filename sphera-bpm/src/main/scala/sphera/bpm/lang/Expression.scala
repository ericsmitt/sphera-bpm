package sphera.bpm.lang

import akka.util.Timeout
import sphera.bpm.Implicits._
import sphera.bpm.storage.definition.{ BooleanDef, DecimalDef, Def, StringDef }
import sphera.core.akkaext.actor._

import scala.concurrent.{ ExecutionContext, Future }

sealed trait Expression {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[Def]
  def getVars: Set[RawPath]
}

sealed trait BinaryOperator extends Expression {
  val exprA: Expression
  val exprB: Expression

  def getVars: Set[RawPath] = exprA.getVars ++ exprB.getVars
}

sealed trait UnaryOperator extends Expression {
  val expr: Expression

  def getVars: Set[RawPath] = expr.getVars
}

sealed trait Literal extends Expression {
  def getVars = Set.empty
}

case class Equal(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      r1 <- evalExprA
      r2 <- evalExprB
    } yield r1.value equals r2.value
  }
}

case class NotEqual(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      r1 <- evalExprA
      r2 <- evalExprB
    } yield !(r1.value equals r2.value)
  }
}

case class GreaterThan(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x.value, y.value) match {
      case (str1: String, str2: String) => str1 > str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 > d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class LowerThan(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x.value, y.value) match {
      case (str1: String, str2: String) => str1 < str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 < d2
      case (d1: Int, d2: BigDecimal) => BigDecimal(d1) < d2
      case (d1: BigDecimal, d2: Int) => d1 < BigDecimal(d2)
      case (d1: Int, d2: Int) => d1 < d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class GreaterThanOrEqual(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x.value, y.value) match {
      case (str1: String, str2: String) => str1 >= str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 >= d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class LowerThanOrEqual(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x.value, y.value) match {
      case (str1: String, str2: String) => str1 <= str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 <= d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Not(expr: Expression) extends UnaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    expr.eval(ctx).map(_.value).map {
      case bool: Boolean => !bool
      case str: String => (str == null) || (str.length == 0)
      case val1: BigDecimal => val1 == BigDecimal(0)
      case _ => false
    }
  }
}

case class Or(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    exprA.eval(ctx).map(_.value).flatMap {
      case val1: Boolean => exprB.eval(ctx).map(_.value).map {
        case val2: Boolean => val1 || val2
        case _ => false
      }
      case _ => Future.successful(false)
    }
  }
}

case class And(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    exprA.eval(ctx).map(_.value).flatMap {
      case val1: Boolean => exprB.eval(ctx).map(_.value).map {
        case val2: Boolean => val1 && val2
        case _ => false
      }
      case _ => Future.successful(false)
    }
  }
}

case class True() extends Literal {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    Future.successful(true)
  }
}

case class False() extends Literal {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[BooleanDef] = {
    Future.successful(false)
  }
}

case class Variable(path: RawPath) extends Expression with NodePathMapper {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[Def] = {
    ctx.storage.getDef(resolve(path, ctx.scopeMappings))
  }
  def getVars = Set(path)
}

case class StringLiteral(value: String) extends Literal {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[StringDef] = {
    Future.successful(value)
  }
}

case class DecimalLiteral(value: BigDecimal) extends Literal {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[DecimalDef] = {
    Future.successful(value)
  }
}

case class Plus(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[Def] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x.value, y.value) match {
      case (str1: String, str2: String) => str1 + str2
      case (d1: BigDecimal, d2: BigDecimal) => d1 + d2
      case (str1: String, d2: BigDecimal) => str1 + d2.toString
      case (d1: Int, d2: BigDecimal) => BigDecimal(d1) + d2
      case (d1: BigDecimal, d2: Int) => d1 + BigDecimal(d2)
      case (d1: Int, d2: Int) => d1 + d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Minus(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[Def] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x.value, y.value) match {
      case (d1: BigDecimal, d2: BigDecimal) => d1 - d2
      case (x: String, y: String) => x.replace(y, "")
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Divide(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[DecimalDef] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x.value, y.value) match {
      case (d1: BigDecimal, d2: BigDecimal) => d1 / d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Multiply(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[Def] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x.value, y.value) match {
      case (d1: BigDecimal, d2: BigDecimal) => d1 * d2
      case (d1: Int, d2: BigDecimal) => BigDecimal(d1) * d2
      case (d1: BigDecimal, d2: Int) => d1 * BigDecimal(d2)
      case (d1: Int, d2: Int) => d1 * d2
      case _ => throw new IllegalArgumentException()
    }
  }
}

case class Remainder(exprA: Expression, exprB: Expression) extends BinaryOperator {
  def eval(ctx: EvaluationContext)(implicit c: ExecutionContext, t: Timeout): Future[Def] = {
    val evalExprA = exprA.eval(ctx)
    val evalExprB = exprB.eval(ctx)
    for {
      x <- evalExprA
      y <- evalExprB
    } yield (x.value, y.value) match {
      case (d1: BigDecimal, d2: BigDecimal) => d1 % d2
      case (d1: Int, d2: BigDecimal) => BigDecimal(d1) % d2
      case (d1: BigDecimal, d2: Int) => d1 % BigDecimal(d2)
      case (d1: Int, d2: Int) => d1 % d2
      case _ => throw new IllegalArgumentException()
    }
  }
}