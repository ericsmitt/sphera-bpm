package sphera.bpm.lang

import sphera.bpm.bpmn.BpmnId
import sphera.bpm.process._

import scala.util.parsing.combinator.{ PackratParsers, RegexParsers }
import scala.util.parsing.input.CharSequenceReader

/**
 * expr := expr '==' term | expr '!=' term | expr '>' term | expr '<' term | expr '>=' term | expr '<=' term | term
 *
 * term := expr OR term1 | expr AND term1 | term1
 *
 * term1 := expr '+' term2 | expr '-' term2 | term2
 *
 * term2 := expr '*' term3 | expr '/' term3 | term3
 *
 * term3 := '(' expr ')' | decimal_literal | string_literal | boolean_literal | identifier
 *
 * decimal_literal := регулярное выражение [0-9][0-9]*[\.]?[0-9]*
 *
 * string_literal := регулярное выражение
 *
 * boolean_literal := TRUE | FALSE
 */
object Compiler extends RegexParsers with PackratParsers {
  private val BEGIN = "begin"
  private val END = "end"
  private val SWITCH = "switch"
  private val CASE = "case"
  private val OTHERWISE = "otherwise"
  private val COLON = ":"
  private val COMMA = ","
  private val GOTO = "goto"
  private val WAIT = "wait"
  private val FOR = "for"
  private val FORK = "fork"
  private val INPUT = "input"
  private val ROLE = "role"
  private val LET = "let"
  private val PROCESS = "processId"
  private val ROLES = "roles"
  private val VAR = "var"
  private val STRING = "string"
  private val INT = "int"
  private val BOOLEAN = "boolean"
  private val DECIMAL = "decimal"
  private val ASSIGNMENT = "="
  private val FORM = "form"
  private val MAIL = "mail"
  private val TO = "to"
  private val SUBJECT = "subject"
  private val MESSAGE = "message"

  private val EQ = "=="
  private val NE = "!="
  private val GT = ">"
  private val GE = ">="
  private val LT = "<"
  private val LE = "<="
  private val OR = "or"
  private val AND = "and"
  private val NOT = "not"
  private val PLUS = "+"
  private val MINUS = "-"
  private val MULTIPLY = "*"
  private val DIVIDE = "/"
  private val REMAINDER = "%"
  private val TRUE = "true"
  private val FALSE = "false"
  private val LEFT_PARENTHESIS = "("
  private val RIGHT_PARENTHESIS = ")"

  def compileExpression(code: String): Either[ParserError, Expression] = {
    val reader = new PackratReader(new CharSequenceReader(code))
    expression(reader) match {
      case NoSuccess(msg, next) => Left(ParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, _) => Right(result)
    }
  }

  def compileExpressions(code: String): Either[ParserError, List[Expression]] = {
    val reader = new PackratReader(new CharSequenceReader(code))
    expressions(reader) match {
      case NoSuccess(msg, next) => Left(ParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, _) => Right(result)
    }
  }

  def compileAssignments(code: String): Either[ParserError, List[Assignment]] = {
    val reader = new PackratReader(new CharSequenceReader(code))
    assignmentStatements(reader) match {
      case NoSuccess(msg, next) => Left(ParserError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, _) => Right(result)
    }
  }

  /*private def enumVariableDefinition: Parser[EnumVariableDefinition] = {
    identifier ~ ENUM ~ LEFT_PARENTHESIS ~ rep1sep(enumElement, COMMA) ~ RIGHT_PARENTHESIS ~ comment ^^ {
      case name ~ _ ~ _ ~ enumElements ~ _ ~ comment =>
        EnumVariableDefinition(name, comment, enumElements)
    }
  }

  private def enumElement: Parser[EnumElementDefinition] = {
    identifier ~ comment ^^ {
      case name ~ comment => EnumElementDefinition(name, comment)
    }
  }*/

  private def prefix: Parser[(String, String)] = {
    identifier ~ opt(comment) ~ COLON ^^ {
      case operation ~ commentOpt ~ _ => (operation, commentOpt.getOrElse(""))
    }

  }

  //  private def inputStatement: Parser[Operation] = {
  //    prefix ~ INPUT ~ FORM ~ stringLiteral ~ role ~ goto ^^ {
  //      case (operation, comment) ~ _ ~ _ ~ StringLiteral(formId) ~ role ~ nextProcess =>
  //        InputOperation(operation, comment, UUID.fromString(formId), nextProcess, role)
  //    }
  //  }

  //  private def switchStatement: Parser[OperationDef] = {
  //    prefix ~ SWITCH ~ rep(caseStatement) ~ opt(otherwiseCondition) ^^ {
  //      case (operation, comment) ~ _ ~ statements ~ otherwise =>
  //        ExclusiveGatewayOperationDef(operation, comment, statements ::: otherwise.map(_ :: Nil).getOrElse(Nil))
  //    }
  //  }

  private def caseStatement: Parser[Branch] = {
    CASE ~ opt(comment) ~ expression ~ goto ^^ {
      case _ ~ commentOpt ~ expr ~ nextOperation =>
        CaseBranch(commentOpt.getOrElse(""), expr, BpmnId(nextOperation))
    }
  }

  private def otherwiseCondition: Parser[Branch] = {
    OTHERWISE ~ opt(comment) ~ goto ^^ {
      case _ ~ commentOpt ~ nextOperation =>
        OtherwiseBranch(commentOpt.getOrElse(""), BpmnId(nextOperation))
    }
  }

  private def letStatement: Parser[OperationDef] = {
    prefix ~ LET ~ rep1sep(assignmentStatement, COMMA) ~ goto ^^ {
      case (operation, comment) ~ _ ~ statements ~ nextOperation =>
        ScriptTaskOperationDef(BpmnId(operation), comment, List.empty, List(BpmnId(nextOperation)), statements, List.empty)
    }
  }

  private def assignmentStatements: Parser[List[Assignment]] = rep1sep(assignmentStatement, COMMA)

  //  private def mailStatement: Parser[Operation] = {
  //    prefix ~ MAIL ~
  //      TO ~ expressions ~
  //      SUBJECT ~ expression ~
  //      MESSAGE ~ expression ~
  //      goto ^^ {
  //        case (operation, comment) ~ _ ~ _ ~ adresses ~ _ ~ subject ~ _ ~ messsage ~ nextOperation =>
  //          UserTaskOperationDef(BpmnId(operation), comment, adresses, subject, messsage, nextOperation)
  //      }
  //  }

  private def expressions: Parser[List[Expression]] = rep1sep(expression, COMMA)

  private def assignmentStatement: Parser[Assignment] =
    (identifier <~ ASSIGNMENT) ~ expression ^^ { case name ~ expr => Assignment(name, expr) }

  private def goto: Parser[String] = GOTO ~> identifier

  private def role: Parser[String] = ROLE ~> identifier

  private lazy val expression: PackratParser[Expression] =
    expression ~ (EQ | NE | GT | GE | LT | LE) ~ term ^^ binOperation | term

  private lazy val term: PackratParser[Expression] = NOT ~ term0 ^^ unaryOperation | term0

  private lazy val term0: PackratParser[Expression] = term0 ~ (OR | AND) ~ term1 ^^ binOperation | term1

  private lazy val term1: PackratParser[Expression] = term1 ~ (PLUS | MINUS) ~ term2 ^^ binOperation | term2

  private lazy val term2: PackratParser[Expression] = term2 ~ (MULTIPLY | DIVIDE | REMAINDER) ~ value ^^ binOperation | value

  private def value: Parser[Expression] = (LEFT_PARENTHESIS ~> expression <~ RIGHT_PARENTHESIS) |
    decimalLiteral | stringLiteral | trueLiteral | falseLiteral | variable

  private def unaryOperation(p: String ~ Expression) = p match {
    case NOT ~ e2 => Not(e2)
    case _ => sys.error("err")
  }

  private def binOperation(p: Expression ~ String ~ Expression) = p match {
    case e1 ~ EQ ~ e2 => Equal(e1, e2)
    case e1 ~ NE ~ e2 => NotEqual(e1, e2)
    case e1 ~ GT ~ e2 => GreaterThan(e1, e2)
    case e1 ~ GE ~ e2 => GreaterThanOrEqual(e1, e2)
    case e1 ~ LT ~ e2 => LowerThan(e1, e2)
    case e1 ~ LE ~ e2 => LowerThanOrEqual(e1, e2)
    case e1 ~ OR ~ e2 => Or(e1, e2)
    case e1 ~ AND ~ e2 => And(e1, e2)
    case e1 ~ PLUS ~ e2 => Plus(e1, e2)
    case e1 ~ MINUS ~ e2 => Minus(e1, e2)
    case e1 ~ MULTIPLY ~ e2 => Multiply(e1, e2)
    case e1 ~ DIVIDE ~ e2 => Divide(e1, e2)
    case e1 ~ REMAINDER ~ e2 => Remainder(e1, e2)
  }

  private def trueLiteral = TRUE ^^ { case _ => True() }
  private def falseLiteral = FALSE ^^ { case _ => False() }
  private def variable = identifier ^^ { case name => Variable(name) }

  private def namePattern = "[a-zA-Zа-яА-Я_][a-zA-Zа-яА-Я0-9_]*"
  private def namespacePattern = "\\$?"

  private def identifier: Parser[String] = {
    s"$namespacePattern$namePattern(\\.$namePattern)*".r ^^ { str => str }
  }

  private def stringLiteral: Parser[StringLiteral] = {
    """"[^"]*"""".r ^^ { str =>
      val content = str.substring(1, str.length - 1)
      StringLiteral(content)
    }
  }

  private def comment: Parser[String] = {
    """'[^']*'""".r ^^ { str =>
      str.substring(1, str.length - 1)
    }
  }

  private def decimalLiteral: Parser[DecimalLiteral] = "-" ~> decimalLiteral ^^ (n => DecimalLiteral(-n.value)) |
    "[0-9][0-9]*[\\.]?[0-9]*".r ^^ { str => DecimalLiteral(BigDecimal(str)) }

}

sealed trait CompilationError

case class LexerError(location: Location, msg: String) extends CompilationError
case class ParserError(location: Location, msg: String) extends CompilationError

case class Location(line: Int, column: Int) {
  override def toString = s"$line:$column"
}
