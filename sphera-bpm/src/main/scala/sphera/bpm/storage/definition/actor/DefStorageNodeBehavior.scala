package sphera.bpm.storage.definition.actor

import akka.pattern.PipeToSupport
import sphera.bpm.{ Bpm, DefNotFoundException, UpdateDefValueException, UpdateDefValueOnlyException }
import sphera.bpm.model.Modifier
import sphera.bpm.storage.Storage.NodeUpdatedTry
import sphera.bpm.storage.definition.DefStorage._
import sphera.bpm.storage.definition._
import sphera.bpm.storage.json.actor.JsonStorageNodeBehavior
import sphera.core.akkaext.actor._
import sphera.bpm.json._
import io.circe.Json

import scala.concurrent.Future

trait DefStorageNodeBehavior[A <: DefStorageLike[A]] extends JsonStorageNodeBehavior with FutureSupport {
  type State <: DefStorageNodeStateLike[A]

  def defStorageBehavior(state: State): Receive = {
    case GetDef(x) => sender() ! DefOpt(state.defStorage.getDefOpt(x))
    case GetDefIntValue(x) => sender() ! DefIntValueOpt(state.defStorage.getDefIntValue(x))
    case GetDefStringValue(x) => sender() ! DefStringValueOpt(state.defStorage.getDefStringValueOpt(x))
    case GetDefBooleanValue(x) => sender() ! DefBooleanValueOpt(state.defStorage.getDefBooleanValueOpt(x))
    case GetDefDecimalValue(x) => sender() ! DefDecimalValueOpt(state.defStorage.getDefDecimalValueOpt(x))
    case UpdateDefValueCmd(x, y, z) => tryUpdate(state, UpdatedDefValueEvt(x, y, z), z)
    case UpdateDefValueOnlyCmd(x, y, z) => tryUpdate(state, UpdatedDefValueOnlyEvt(x, y, z), z)
    case CreateDefCmd(x, y, z) => tryUpdate(state, CreatedDefEvt(x, y, z), z)
    case UpdateDefCmd(x, y, z) => tryUpdate(state, UpdatedDefEvt(x, y, z), z)
    case DeleteDefCmd(x, y, z) => tryUpdate(state, DeletedDefEvt(x, y, z), z)
    case FindRoleUser(x) => sender() ! RoleUserOpt(state.defStorage.findRoleUser(x))
  }
}