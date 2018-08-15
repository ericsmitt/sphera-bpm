package sphera.bpm.lang

import akka.event.LoggingAdapter
import sphera.bpm.Implicits._
import sphera.bpm.model.ImplicitModifier
import sphera.bpm.process.Assignment
import sphera.bpm.storage.Node
import sphera.bpm.storage.definition.Def
import sphera.bpm.{ AssignmentEvaluationException, ExpressionEvaluationException }
import sphera.core.akkaext.actor._

import scala.concurrent.Future

/**
 * = Expression evaluator =
 *
 * Выражения выполняются в контексте [[EvaluationContext]]
 */
trait Evaluator extends CqrsActorBase
  with NodePathMapper
  with FutureSupport
  with ImplicitModifier {

  implicit def log: LoggingAdapter

  def evalAssignment(assignment: Assignment, ctx: EvaluationContext): Future[Node.Id] = {
    val path = resolve(path = assignment.path, mappings = ctx.scopeMappings)
    evalExpression(assignment.expr, ctx)
      .flatMap(ctx.storage.updateDefValueOnly(path, _))
      .recover {
        case e: Throwable =>
          val evalException = AssignmentEvaluationException(
            assignment = assignment,
            scopeMappings = ctx.scopeMappings,
            cause = e)

          e.printStackTrace()

          self ! evalException

          throw evalException
      }
  }

  def evalAssignments(assignments: Assignments, ctx: EvaluationContext): Future[Node.Id] = {
    assignments match {
      case x :: Nil => evalAssignment(x, ctx)
      case x :: xs => xs.foldLeft(evalAssignment(x, ctx)) {
        case (acc, assignment) => acc flatMap { _ => evalAssignment(assignment, ctx) }
      }
      case Nil => Future.successful(ActorId("sdfsd"))
    }
  }

  def evalExpression(expr: Expression, ctx: EvaluationContext): Future[Def] = {
    expr.eval(ctx).recover {
      case e: Throwable =>
        val evalException = ExpressionEvaluationException(
          expr = expr,
          scopeMappings = ctx.scopeMappings,
          cause = e)

        e.printStackTrace()

        self ! evalException

        throw evalException
    }
  }

  def evalExpressions(exprs: Expressions, ctx: EvaluationContext): Future[List[Def]] = {
    exprs.foldLeft(Future.successful(List[Def]())) {
      case (acc, expr) => acc flatMap { result =>
        evalExpression(expr, ctx).map(result :+ _)
      }
    }
  }

  def evalString(expr: Expression, ctx: EvaluationContext): Future[String] = evalExpression(expr, ctx).map(_.toString)

  def evalStringList(exprs: Expressions, ctx: EvaluationContext): Future[List[String]] = {
    evalExpressions(exprs, ctx)
      .map(_.map(_.toString))
  }
}