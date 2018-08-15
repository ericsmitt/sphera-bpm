package sphera.bpm

import java.time.ZonedDateTime
import java.util.UUID

import sphera.bpm.bpmn.{BpmnId, SequenceFlow}
import sphera.bpm.lang.{Expression, Variable}
import sphera.bpm.masterdata.model.DataStructureElement
import sphera.bpm.model._
import sphera.bpm.process.{Assignment, Operation}
import sphera.bpm.runnable.Runnable
import sphera.bpm.storage.Node
import sphera.bpm.task.{Task, TaskInfo}
import sphera.core.akkaext.actor._
import sphera.bpm.json._
import sphera.bpm.storage.definition.{Def, PatchDef}
import io.circe.Json

sealed trait BpmException extends RuntimeException {
  val cause: Option[BpmException] = None
  val zonedDateTime: ZonedDateTime

  def message: String

  override def getMessage = message
  override def getCause = cause.orNull
}

case class UnknownException(message: String, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException

case class EntityNotFoundException(entityId: Entity.Id, entityType: EntityType, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message = s"$entityType $entityId not found"
}

object EntityNotFoundException {
  def apply(entityId: UUID, entityType: EntityType): EntityNotFoundException =
    new EntityNotFoundException(entityId.toString, entityType)
}

case class EntityAlreadyExistsException(entityId: Entity.Id, entityType: EntityType, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message = s"$entityType $entityId already exists"
}

object EntityAlreadyExistsException {
  def apply(entityId: UUID, entityType: EntityType): EntityAlreadyExistsException =
    new EntityAlreadyExistsException(entityId.toString, entityType)
}

trait BpmRuntimeException extends BpmException

case class RunnableNotFoundException(runnableId: Runnable.Id, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmRuntimeException {
  val message = s"Runnable $runnableId doesn't exist"
}

case class InvalidActivationException(from: Operation.BpmnId, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmRuntimeException {
  val message = s"Invalid activation from $from"
}

case class InvalidCompleteTaskException(taskInfo: TaskInfo, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmRuntimeException {
  val message = taskInfo.asJsonStr
}

case class OperationNotFoundException(op: Operation.BpmnId, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmRuntimeException {
  val message = s"Operation [$op] doesn't exist"
}

trait BpmnParserException extends BpmException

case class OperationTemplateDataException(ops: Set[Operation.BpmnId], zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException with BpmnParserException {
  val message = ops.map(op => s"Invalid operation template data for $op").mkString("\n")
}

object OperationTemplateDataException {
  def apply(op: Operation.BpmnId) = new OperationTemplateDataException(Set(op))
}

case class AttributeNoteFoundException(nodeName: String, attrName: String, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmnParserException {
  val message = s"Node $nodeName: attr $attrName not found"
}

case class OutgoingsNotFoundException(op: Operation.BpmnId, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmnParserException {
  val message = s"$op не имеет выходов"
}

case class SeqFlowExpressionNotFound(seqFlowId: SequenceFlow.Id, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmnParserException {
  val message = s"SequenceFlow $seqFlowId: отсутствует выражение"
}

case class SeqFlowNotFound(seqFlowId: SequenceFlow.Id, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmnParserException {
  val message = s"SequenceFlow $seqFlowId не существует"
}

case class CompileExpressionException(
  elementType: String,
  elementId: String,
  compileErrorMessage: String,
  compileErrorLocation: String,
  zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmnParserException {
  val message =
    s"""$elementType [id: $elementId]:
       |ошибка компиляции выражения:
       |$compileErrorMessage ($compileErrorLocation).""".stripMargin
}

case class SeqFlowSourceNotFoundException(seqFlowId: SequenceFlow.Id, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmnParserException {
  val message = s"""SequenceFlow "$seqFlowId" не имеет предшественника""".stripMargin
}

case class SeqFlowTargetNotFoundException(seqFlowId: SequenceFlow.Id, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmnParserException {
  val message = s"""SequenceFlow "$seqFlowId" не имеет последователя""".stripMargin
}

case class BpmnParserExceptions(exceptions: List[BpmnParserException], zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message =
    s"""Found ${exceptions.size} errors:
      |${exceptions.map(message => s" - $message").mkString("\n")}""".stripMargin
}

case class CreateRunnableException(x: Seq[BpmException], zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message = "Create exception"
}

case class OperationException(
  opId: Operation.Id,
  opBpmnId: BpmnId,
  opName: Operation.Name,
  override val cause: Option[BpmException],
  zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message = s"Operation [opBpmnId: $opBpmnId, opId: $opId] exception"
}

trait StorageException extends BpmException

case class NodeNotFoundException(nodeId: Node.Id, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends StorageException {
  val path = Path(nodeId)
  val message = s"Node $nodeId not found"
}

case class NodeAlreadyExistsException(nodeId: Node.Id, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends StorageException {
  val path = Path(nodeId)
  val message = s"Node $nodeId already exists"
}

trait DefStorageException extends StorageException

case class DefDecoderException(path: PathLike, decoderMessage: String, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends DefStorageException {
  val message = s"Def [path: $path] decoding from json failed: $decoderMessage"
}

case class DefNotFoundException(path: PathLike, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends DefStorageException {
  val message = s"Def [path: $path] does not exist"
}

case class DefValueNotFoundException(path: PathLike, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends DefStorageException {
  val message = s"Def value [path: $path] does not exist"
}

case class RoleNotFoundException(nodeId: Node.Id, role: String, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends DefStorageException {
  def path = Path(nodeId)
  val message = s"""Role "$role" does not exist, node [$nodeId]""".stripMargin
}

case class UpdateDefValueException(path: PathLike, json: Json, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends DefStorageException {
  val message = s"RefDef [path: $path] try update with [json: $json]"
}

case class UpdateDefValueOnlyException(path: PathLike, definition: Def, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends DefStorageException {
  val message = s"RefDef [path: $path] try update with [definition: $definition]"
}

case class PatchDefException(path: PathLike, patch: PatchDef, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends DefStorageException {
  val message = s"RefDef [path: $path] try patch with [patch: $patch]"
}

trait JsonStorageException extends StorageException

case class RequiredArrayOrObjectException(path: DataPath, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends JsonStorageException {
  val message = s"Required array or object [path: $path]"
}

case class JsonValueNotFoundException(path: DataPath, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends JsonStorageException {
  val message = s"Json value [path: $path] does not exist"
}

case class JsonIsNotJsonObjectException(json: Json, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message = s"Json is not json object [$json]"
}

case class InvalidJsonIsNotJsonObjectException(json: Json, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message = s"Json is not json object [$json]"
}

trait DocumentStorageException extends DefStorageException

case class DocumentNotFoundException(path: PathLike, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends DefStorageException {
  val message = s"Document [path: $path] does not exist"
}

case class VerifyException(zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message = s"Verify exception"
}

case class UnknownDataStructureElementException(element: DataStructureElement, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message = s"Unknown DataStructureElement $element"
}

case class DefStorageBuilderException(message: String, zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException

case class SchemaValidationException(
  messages: List[String],
  override val cause: Option[BpmException],
  zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends BpmException {
  val message = Map("errors" -> messages.asJson).asJson.pretty(printer)
}

trait EvaluationException extends BpmException {
  def scopeMappings: Map[RawPath, ActorId]
}

case class AssignmentEvaluationException(
                                assignment: Assignment,
                                scopeMappings: Map[RawPath, ActorId],
                               override val cause: Option[BpmException],
                               zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends EvaluationException {
  val message = Map(
    "assignment" -> assignment.asJson,
    "scopeMappings" -> scopeMappings.asJson,
  ).asJson.pretty(printer)
}

case class ExpressionEvaluationException(
                                          expr: Expression,
                                          scopeMappings: Map[RawPath, ActorId],
                                          override val cause: Option[BpmException],
                                          zonedDateTime: ZonedDateTime = ZonedDateTime.now()) extends EvaluationException {
  val message = Map(
    "expr" -> expr.asJson,
    "scopeMappings" -> scopeMappings.asJson,
  ).asJson.pretty(printer)
}
