package sphera.bpm.bpmn

import sphera.bpm.Implicits._
import sphera.bpm._
import sphera.bpm.lang.{ Compiler, Expression }
import sphera.bpm.process._
import sphera.core.akkaext.actor.FutureSupport
import sphera.core.utils.Generator
import io.circe.Json

import scala.concurrent.Future
import scala.xml.{ Node, XML }

trait BpmnParser extends FutureSupport with Generator {
  def bpm: Bpm

  def parse(sourceCode: String, data: Set[OperationTemplateData], parentBpmnId: Option[Process.BpmnId] = None): Future[Either[List[BpmnParserException], BpmnParserResult]] = {
    val refinedCode = sourceCode.replaceAll("<\\?[\\s\\S]*?\\?>", "")
    val xml = XML.loadString(refinedCode)
    val node = (xml \ "process").head
    val attr = node.attribute("id").map(_.text).getOrElse(generateString())
    val nodes = node.child.filter(!_.isAtom)
    parseNodes(data, nodes) map { _.map(x => BpmnParserResult(BpmnId(attr), x.toSet)) }
  }

  def mapNode(node: Node): Option[(String, Node)] = {
    node.attribute("id")
      .map(_.text)
      .map(id => id -> node)
  }

  def parseNodes(data: Set[OperationTemplateData], nodes: Seq[Node]): Future[Either[List[BpmnParserException], List[OperationDef]]] = {
    val nodesMap: Map[String, Node] = nodes.flatMap(mapNode).toMap
    val result: Future[Seq[Option[Either[List[BpmnParserException], OperationDef]]]] = Future.sequence {
      nodes.map { node =>
        node.label match {
          case "startEvent" => startEvent(data, node, nodesMap).map(Some.apply)
          case "endEvent" => endEvent(data, node, nodesMap).map(Some.apply)
          case "parallelGateway" => parallelGateway(data, node, nodesMap).map(Some.apply)
          case "exclusiveGateway" => exclusiveGateway(data, node, nodesMap).map(Some.apply)
          case "scriptTask" => scriptTask(data, node, nodesMap).map(Some.apply)
          case "sendTask" => sendTask(data, node, nodesMap).map(Some.apply)
          case "userTask" => userTask(data, node, nodesMap).map(Some.apply)
          case "subProcess" => subProcess(data, node, nodesMap).map(Some.apply)
          case _ => Future.successful(None)
        }
      }
    }
    result.map(_.flatten) map { x =>
      val hasError = x.exists(_.isLeft)
      if (hasError) {
        val errors = x.flatMap(r => r.left.getOrElse(List.empty)).toList
        Left(errors)
      } else {
        val operations = x.flatMap(r => r.right.toOption).toList
        Right(operations)
      }
    }
  }

  case class ParseNodeResult(
    bpmnId: BpmnId,
    name: String,
    in: List[BpmnId],
    out: List[Operation.BpmnId],
    incoming: List[String],
    outgoing: List[String],
    nextOpsRes: List[Either[BpmnParserException, String]],
    prevOpsRes: List[Either[BpmnParserException, String]])

  def parseNode(node: Node, nodes: Map[String, Node], splitAssignments: Boolean = false): Future[Either[List[BpmnParserException], ParseNodeResult]] = {
    Future {
      val label = node.label
      val nameRes = getAttributeTry(node, "name")
      val bpmnIdRes = getAttributeTry(node, "id")
      val incoming: List[String] = (node \ "incoming").map(_.text).toList
      val outgoing: List[String] = (node \ "outgoing").map(_.text).toList
      val nextOpsRes: List[Either[BpmnParserException, String]] = outgoing.map(outgoingId => getSeqFlowTargetRef(outgoingId, nodes))
      val prevOpsRes: List[Either[BpmnParserException, String]] = incoming.map(id => getSeqFlowSourceRef(id, nodes))

      val outgoingRes =
        if (outgoing.isEmpty && label != "endEvent") List(OutgoingsNotFoundException(bpmnIdRes.right.get))
        else List.empty

      val errors: List[BpmnParserException] = {
        nextOpsRes.flatMap(_.left.toOption) ++
          prevOpsRes.flatMap(_.left.toOption) ++
          bpmnIdRes.left.toSeq ++
          nameRes.left.toSeq ++
          outgoingRes
      }

      if (errors.nonEmpty) Left(errors)
      else {
        Right(ParseNodeResult(
          bpmnId = bpmnIdRes.right.get,
          name = nameRes.right.get,
          in = prevOpsRes.map(x => BpmnId(x.right.get)),
          out = nextOpsRes.map(x => BpmnId(x.right.get)),
          incoming = incoming,
          outgoing = outgoing,
          nextOpsRes = nextOpsRes,
          prevOpsRes = prevOpsRes))
      }
    }
  }

  def startEvent(
    data: Set[OperationTemplateData],
    node: Node,
    nodes: Map[String, Node]): Future[Either[List[BpmnParserException], StartOperationDef]] = {
    parseNode(node, nodes) map { params =>
      params flatMap { x =>
        data.find(_.bpmnId == x.bpmnId) match {
          case Some(data: StartOperationTemplateData) =>
            Right(StartOperationDef(
              bpmnId = x.bpmnId,
              name = x.name,
              out = x.out,
              assignments = data.assignments))
          case _ =>
            Left(List(OperationTemplateDataException(x.bpmnId)))
        }
      }
    }
  }

  def endEvent(
    data: Set[OperationTemplateData],
    node: Node,
    nodes: Map[String, Node]): Future[Either[List[BpmnParserException], EndOperationDef]] = {
    parseNode(node, nodes) map { params =>
      params flatMap { x =>
        data.find(_.bpmnId == x.bpmnId) match {
          case Some(data: EndOperationTemplateData) =>
            Right(EndOperationDef(
              bpmnId = x.bpmnId,
              name = x.name,
              in = x.in,
              assignments = data.assignments))
          case _ =>
            Left(List(OperationTemplateDataException(x.bpmnId)))
        }
      }
    }
  }

  def parallelGateway(
    data: Set[OperationTemplateData],
    node: Node,
    nodes: Map[String, Node]): Future[Either[List[BpmnParserException], ParallelGatewayOperationDef]] = {
    parseNode(node, nodes) map { params =>
      params flatMap { x =>
        data.find(_.bpmnId == x.bpmnId) match {
          case Some(data: ParallelGatewayOperationTemplateData) =>
            Right(ParallelGatewayOperationDef(
              bpmnId = x.bpmnId,
              name = x.name,
              in = x.in,
              out = x.out,
              assignments = data.assignments))
          case _ =>
            Left(List(OperationTemplateDataException(x.bpmnId)))
        }
      }
    }
  }

  def exclusiveGateway(
    data: Set[OperationTemplateData],
    node: Node,
    nodes: Map[String, Node]): Future[Either[List[BpmnParserException], ExclusiveGatewayOperationDef]] = {
    parseNode(node, nodes) map { params =>
      params flatMap { x =>
        data.find(_.bpmnId == x.bpmnId) match {
          case Some(data: ExclusiveGatewayOperationTemplateData) =>
            if (x.out.nonEmpty) {
              val maybeDefaultSeqFlowId = getAttribute(node, "default")
              val outgoingIds = x.outgoing
              val seqFlowAndTargetIds = outgoingIds.map(id => id -> getSeqFlowTargetRef(id, nodes))
              val caseBranchesRes = seqFlowAndTargetIds.map {
                case (seqFlowId, Right(targetId)) =>
                  val default = maybeDefaultSeqFlowId.exists(_.equals(seqFlowId))
                  if (default) Right(OtherwiseBranch("", BpmnId(targetId))) // TODO: описание ветки не должно быть пусто
                  else caseBranch(data, seqFlowId, targetId, nodes)
              }
              val caseBranchErrors = caseBranchesRes.flatMap(_.left.toOption)
              if (caseBranchErrors.isEmpty) {
                val conditions = caseBranchesRes.flatMap(_.right.toOption)
                Right(ExclusiveGatewayOperationDef(
                  bpmnId = x.bpmnId,
                  name = x.name,
                  in = x.in,
                  out = x.out,
                  conditions = conditions,
                  assignments = data.assignments))
              } else Left(caseBranchErrors)
            } else Left(List(OutgoingsNotFoundException(x.bpmnId)))
          case _ =>
            Left(List(OperationTemplateDataException(x.bpmnId)))
        }
      }
    }
  }

  def caseBranch(data: ExclusiveGatewayOperationTemplateData, seqFlowId: String, targetId: String, elementMap: Map[String, Node]): Either[BpmnParserException, CaseBranch] = {
    data.sequenceflowExpr.get(seqFlowId)
      .map(expr => Right(CaseBranch(seqFlowId, expr, targetId)))
      .getOrElse {
        elementMap
          .get(seqFlowId)
          .map(_ => Left(SeqFlowExpressionNotFound(seqFlowId)))
          .getOrElse(Left(SeqFlowNotFound(seqFlowId)))
      }
  }

  def scriptTask(
    data: Set[OperationTemplateData],
    node: Node,
    nodes: Map[String, Node]): Future[Either[List[BpmnParserException], ScriptTaskOperationDef]] = {
    parseNode(node, nodes) map { params =>
      params flatMap { x =>
        data.find(_.bpmnId == x.bpmnId) match {
          case Some(data: ScriptTaskOperationTemplateData) =>
            Right(ScriptTaskOperationDef(
              bpmnId = x.bpmnId,
              name = x.name,
              in = x.in,
              out = x.out,
              preAssignments = data.preAssignments,
              postAssignments = data.postAssignments))
          case _ =>
            Left(List(OperationTemplateDataException(x.bpmnId)))
        }
      }
    }
  }

  def sendTask(
    data: Set[OperationTemplateData],
    node: Node,
    nodes: Map[String, Node]): Future[Either[List[BpmnParserException], SendTaskOperationDef]] = {
    parseNode(node, nodes) map { params =>
      params flatMap { x =>
        data.find(_.bpmnId == x.bpmnId) match {
          case Some(data: SendTaskOperationTemplateData) =>
            Right(SendTaskOperationDef(
              bpmnId = x.bpmnId,
              name = x.name,
              recipients = data.recipients,
              subject = data.subject,
              message = data.message,
              in = x.in,
              out = x.out,
              preAssignments = data.preAssignments,
              postAssignments = data.postAssignments))
          case _ =>
            Left(List(OperationTemplateDataException(x.bpmnId)))
        }
      }
    }
  }

  def userTask(
    data: Set[OperationTemplateData],
    node: Node,
    nodes: Map[String, Node]): Future[Either[List[BpmnParserException], UserTaskOperationDef]] = {
    parseNode(node, nodes) map { params =>
      params flatMap { x =>
        data.find(_.bpmnId == x.bpmnId) match {
          case Some(data: UserTaskOperationTemplateData) =>
            Right(UserTaskOperationDef(
              bpmnId = x.bpmnId,
              name = x.name,
              role = data.role,
              assigneeId = data.assigneeId,
              reviewerId = data.reviewerId,
              watcherIds = data.watcherIds,
              formTemplateId = data.formTemplateId,
              dataStructureId = data.dataStructureId,
              plannedStart = data.plannedStart,
              plannedDuration = data.plannedDuration,
              in = x.in,
              out = x.out,
              preAssignments = data.preAssignments,
              postAssignments = data.postAssignments))
          case _ =>
            Left(List(OperationTemplateDataException(x.bpmnId)))
        }
      }
    }
  }

  def subProcess(
    data: Set[OperationTemplateData],
    node: Node,
    nodes: Map[String, Node]): Future[Either[List[BpmnParserException], SubProcessOperationDef]] = {
    parseNode(node, nodes) map { params =>
      params flatMap { x =>
        data.find(_.bpmnId == x.bpmnId) match {
          case Some(data: SubProcessOperationTemplateData) =>
            Right(SubProcessOperationDef(
              bpmnId = x.bpmnId,
              name = x.name,
              data = data.data,
              templateId = data.templateId,
              taskDataStructureId = data.taskDataStructureId,
              plannedStart = data.plannedStart,
              plannedDuration = data.plannedDuration,
              in = x.in,
              out = x.out,
              preAssignments = data.preAssignments,
              postAssignments = data.postAssignments))
          case _ =>
            Left(List(OperationTemplateDataException(x.bpmnId)))
        }
      }
    }
  }

  def compileExpression[A](
    code: String,
    elementType: String,
    elementId: String)(func: Expression => A): Either[BpmnParserException, A] = {
    Compiler.compileExpression(code) match {
      case Right(expression) => Right(func(expression))
      case Left(compileError) => Left(CompileExpressionException(
        elementType = elementId,
        elementId = elementType,
        compileErrorMessage = compileError.msg,
        compileErrorLocation = compileError.location.toString))
    }
  }

  def compileExpressions[A](
    code: String,
    elementType: String,
    elementId: String)(func: List[Expression] => A): Either[BpmnParserException, A] = {
    Compiler.compileExpressions(code) match {
      case Right(expressions) => Right(func(expressions))
      case Left(compileError) => Left(CompileExpressionException(
        elementType = elementId,
        elementId = elementType,
        compileErrorMessage = compileError.msg,
        compileErrorLocation = compileError.location.toString))
    }
  }

  def getAttribute(node: Node, attr: String): Option[String] = node.attribute(attr).map(_.text)

  def getAttributeTry(node: Node, attr: String): Either[AttributeNoteFoundException, String] = getAttribute(node, attr) match {
    case Some(x) => Right(x)
    case None => Left(AttributeNoteFoundException(node.label, attr))
  }

  def getSeqFlowSourceRef(seqFlowId: String, elementMap: Map[String, Node]): Either[BpmnParserException, String] =
    getSeqFlowRef(seqFlowId, isSource = true, elementMap)

  def getSeqFlowTargetRef(seqFlowId: String, elementMap: Map[String, Node]): Either[BpmnParserException, String] =
    getSeqFlowRef(seqFlowId, isSource = false, elementMap)

  def getSeqFlowRef(seqFlowId: String, isSource: Boolean, elementMap: Map[String, Node]): Either[BpmnParserException, String] = {
    val maybeSeqNode = elementMap.get(seqFlowId)
    maybeSeqNode.map {
      seqNode =>
        val maybeElementId = if (isSource) getAttribute(seqNode, "sourceRef")
        else getAttribute(seqNode, "targetRef")
        maybeElementId
          .map {
            elementId => Right(elementId)
          }
          .getOrElse {
            if (isSource) Left(SeqFlowSourceNotFoundException(seqFlowId))
            else Left(SeqFlowTargetNotFoundException(seqFlowId))
          }

    }.getOrElse(Left(SeqFlowNotFound(seqFlowId)))
  }
}

case class BpmnParserResult(bpmnId: Process.BpmnId, opDefs: Set[OperationDef])
