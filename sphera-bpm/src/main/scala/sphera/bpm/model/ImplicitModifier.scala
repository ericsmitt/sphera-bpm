package sphera.bpm.model

import sphera.bpm.process.actor.ProcessActor
import sphera.bpm.process.actor.operation.OperationActorLike
import sphera.bpm.task.actor.TaskActor
import sphera.core.akkaext.actor.CqrsActorBase
import sphera.core.utils.Generator

trait ImplicitModifier extends Generator {
  def modifierType = this match {
    case x: OperationActorLike => ModifierType.Operation
    case x: ProcessActor => ModifierType.Process
    case x: TaskActor[_] => ModifierType.Task
  }

  implicit val modifier = this match {
    case x: CqrsActorBase => Modifier(actorId = x.id, modifierType = modifierType)
    case _ => Modifier(generateUUID)
  }
}